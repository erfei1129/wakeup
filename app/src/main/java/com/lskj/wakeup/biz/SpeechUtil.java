package com.lskj.wakeup.biz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ge Xiaodong
 * @time 2019/9/25 16:06
 * @description 讯飞语音唤醒+识别
 */
public class SpeechUtil {

    private static final String TAG = "SpeechUtil";
    // ------------------------------------------------唤醒+识别初始化内容----------------------------------------
    private Context mContext;
    private List<VoiceResultListener> mVoiceResultListenerList;

    public void addVoiceResultListener(VoiceResultListener listener) {
        mVoiceResultListenerList.add(listener);
    }

    public void removeVoicceResultListener(VoiceResultListener listener) {
        mVoiceResultListenerList.remove(listener);
    }

    // 唤醒的阈值，就相当于门限值，当用户输入的语音的置信度大于这一个值的时候，才被认定为成功唤醒。
    private int curThresh = 1000;

    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    // 语音识别对象
    private SpeechRecognizer mAsr;

    // 本地语法id
    private String mLocalGrammarID;
    // 本地语法文件
    private String mLocalGrammar;
    // 本地语法构建路径
    private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/lskj";

    // 唤醒结果
    private String resultString;
    private List<AsrResultModel> asrResultList;
    // 唤醒识别的结果类型
    private int asrCurrentType = -1;

    // ------------------------------------------------语音合成初始化内容----------------------------------------
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 音库
    private String voiceLocal = "xiaoyan";

    // 单例
    @SuppressLint("StaticFieldLeak")
    private static SpeechUtil mSpeechHelper;

    public static void init(Context context) {
        if (mSpeechHelper == null) {
            synchronized (SpeechUtil.class) {
                if (mSpeechHelper == null) {
                    mSpeechHelper = new SpeechUtil(context);
                }
            }
        }
    }

    public static SpeechUtil getInstance() {
        return mSpeechHelper;
    }

    private SpeechUtil(Context context) {
        this.mContext = context;
        asrResultList = new ArrayList<>();
        mVoiceResultListenerList = new ArrayList<>();

        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(mContext, code -> {
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码：" + code);
            }
        });
        // 初始化识别对象---唤醒+识别,用来构建语法
        mAsr = SpeechRecognizer.createRecognizer(mContext, code -> {
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码：" + code);
            }
        });
        // 初始化语法文件
        mLocalGrammar = readFile("wake.bnf", "utf-8");

        mTts = SpeechSynthesizer.createSynthesizer(mContext, code -> {
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码：" + code);
            }
        });

        initAsrAndStartWakeuper();
    }


    // 初始化语法识别成功后,开始唤醒监听
    private void initAsrAndStartWakeuper() {
        mAsr = SpeechRecognizer.getRecognizer();
        if (null != mAsr) {
            mAsr.setParameter(SpeechConstant.PARAMS, null);
            mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            // 设置引擎类型
            mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置语法构建路径
            mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
            // 设置资源路径
            mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getCommonResourcePath());
            int ret = mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener);
            if (ret != ErrorCode.SUCCESS) {
                Log.e(TAG, "语法构建失败,错误码：" + ret);
            }
        }
    }

    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mLocalGrammarID = grammarId;
                Log.d(TAG, "语法构建成功：" + grammarId);

                startWakeuper();
            } else {
                Log.d(TAG, "语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    // 开启唤醒功能
    public void startWakeuper() {
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
            asrResultList.clear();

            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 设置唤醒模式------------唤醒+识别
            mIvw.setParameter(SpeechConstant.IVW_SST, "oneshot");
            //设置识别引擎，只影响唤醒后的识别（唤醒本身只有离线类型）
            mIvw.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            String resPath = ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "ivw/5db263d2.jet");

            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置持续进行唤醒  单独做唤醒时使用
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, resPath);
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            if (!TextUtils.isEmpty(mLocalGrammarID)) {
                // 设置本地识别资源
                mIvw.setParameter(ResourceUtil.ASR_RES_PATH, getCommonResourcePath());
                // 设置语法构建路径
                mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
                // 设置本地识别使用语法id
                mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR, mLocalGrammarID);
                // 启动唤醒
                mIvw.startListening(mWakeuperListener);
            } else {
                Log.d(TAG, "请先构建语法");
            }
        }
    }

    // 销毁唤醒功能
    public void destroyWakeuper() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
    }

    // 停止唤醒
    public void stopWakeuper() {
        mIvw.stopListening();
    }

    //  唤醒词监听类
    private WakeuperListener mWakeuperListener = new WakeuperListener() {


        //开始说话
        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "开始说话");
        }

        //错误码返回
        @Override
        public void onError(SpeechError arg0) {
            startMixSpeech(-1);
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            Log.d(TAG, "eventType:" + eventType + ", arg1:" + isLast + ", arg2:" + arg2);
            // 识别结果
            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
                RecognizerResult reslut = ((RecognizerResult) obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
                assert reslut != null;
                asrCurrentType = parseGrammarResult(reslut.getResultString());
                // 得到命令词识别结果,开始合成语音播报内容
                startMixSpeech(asrCurrentType);
            }
        }

        @Override
        public void onVolumeChanged(int i) {

        }

        @SuppressLint("ResourceType")
        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();

                uploadRecognition(text);
                // 播放音频
//                MediaPlayUtil.startRawPlay(mContext, R.raw.wakeup);
                // 亮屏
//                DeviceUtil.getInstance().unlockScreen();

                Log.d(TAG, resultString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 有声音播放,显示对应内容
    public void startMixSpeech(int asrType) {
        asrCurrentType = asrType;
        if (asrType >= 0 && mVoiceResultListenerList != null && mVoiceResultListenerList.size() > 0) {
            for (VoiceResultListener voiceResultListener : mVoiceResultListenerList) {
                voiceResultListener.receiveRecognition(asrType);
            }
        }

        // 识别出询问天气
        switch (asrType) {
            case SpeechContent.TYPE_ALL:
                Log.d(TAG, "闹钟响了,播报所有");
                // 闹钟响了1.先关闭其他播报,2.关闭唤醒监听3.播报闹钟内容
//                stopSpeak();
//                stopWakeuper();
                startMixAndPlay(SpeechContent.TYPE_ALL);
                break;
            case SpeechContent.TYPE_WEATHER:
                Log.d(TAG, "询问了天气");

                startMixAndPlay(SpeechContent.TYPE_WEATHER);
                break;
            case SpeechContent.TYPE_DATE:
                Log.d(TAG, "询问了日期");
                startMixAndPlay(SpeechContent.TYPE_DATE);
                break;
            case SpeechContent.TYPE_LIMIT:
                Log.d(TAG, "询问了限号");
                startMixAndPlay(SpeechContent.TYPE_LIMIT);
                break;
            case SpeechContent.TYPE_TEMP:
                Log.d(TAG, "询问了温湿度");
                startMixAndPlay(SpeechContent.TYPE_TEMP);
                break;
            default:
                Log.d(TAG, "没有匹配结果");
                // TODO 没有结果,重新开启唤醒监听
                // 这个handler换成APPUtils
                new Handler().postDelayed(this::startWakeuper, 2000);
                break;
        }
    }

    // 停止语音播放
    public void stopSpeak() {
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
    }

    // 开始语音合成
    private void startMixAndPlay(int index) {
        List<String> speakContentList = SpeechContent.getInstance().getSpeakContentList();

        int code = mTts.startSpeaking(speakContentList.get(index), mTtsListener);
        // 设置语音合成参数
        setSpeechMixParam();
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG, "语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    // 合成回调监听。
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.d(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakResumed() {
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.d(TAG, "播放完成");
            } else {
                Log.e(TAG, error.getPlainDescription(true));
            }

            // ----------------语音合成播放完成之后,重新开启唤醒监听------------
            // TODO 播放完成,重新开启唤醒监听
            // 这个handler换成APPUtils

            // 播放完成回调
            if (asrCurrentType >= 0 && mVoiceResultListenerList != null && mVoiceResultListenerList.size() > 0) {
                for (VoiceResultListener voiceResultListener : mVoiceResultListenerList) {
                    voiceResultListener.speakComplete(asrCurrentType);
                }
            }

            new Handler().postDelayed(() -> startWakeuper(), 500);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    // 语音合成参数设置
    private void setSpeechMixParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getMixResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voiceLocal);
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    //获取发音人资源路径
    private String getMixResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + voiceLocal + ".jet"));
        return tempBuffer.toString();
    }

    // 上传识别的结果
    private void uploadRecognition(String text) {
//        RecognitionUpload recognition = new RecognitionUpload();
//        recognition.setWord(text);
//        BusinessReqBody reqBody = new BusinessReqBody();
//        reqBody.setBusinessType(TCPBizType.SoftwareBizType.TYPE_SPEECH_RECOGNITION);
//        reqBody.setToken(DyxcApplication.getInstance().getTcpToken());
//        reqBody.setRequestJson(JSON.toJSONString(recognition));
//
//        DyxcApplication.getInstance().packageAndSendTCPMsg(TCPBizType.TYPE_TCP_MSG_BIZ_REQ, JSON.toJSONString(reqBody));
    }

    // 读取asset目录下文件。
    public String readFile(String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 获取识别资源路径
    private String getCommonResourcePath() {
        // 识别通用资源
        return ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet");
    }

    public int parseGrammarResult(String json) {
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for (int j = 0; j < items.length(); j++) {
                    JSONObject obj = items.getJSONObject(j);
                    if (obj.getString("w").contains("nomatch")) {
                        return -1;
                    } else {
                        AsrResultModel resultModel = new AsrResultModel();
                        resultModel.setResultText(obj.getString("w"));
                        resultModel.setResultScore(obj.getInt("sc"));
                        asrResultList.add(resultModel);
                    }
                }
            }
            if (matchText(SpeechContent.matchDate)) {
                return SpeechContent.TYPE_DATE;
            } else if (matchText(SpeechContent.matchWeather)) {
                return SpeechContent.TYPE_WEATHER;
            } else if (matchText(SpeechContent.matchLimit)) {
                return SpeechContent.TYPE_LIMIT;
            } else if (matchText(SpeechContent.matchTemp)) {
                return SpeechContent.TYPE_TEMP;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    // 匹配识别结果
    private boolean matchText(String[] matchContent) {
        if (asrResultList.size() > 0) {
            for (AsrResultModel resultModel : asrResultList) {
                for (String s : matchContent) {
                    if (resultModel.getResultText().contains(s) && resultModel.getResultScore() >= SpeechContent.RECOGNITION_SCORE) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    // 销毁语音合成
    public void destroySpeechMix() {
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }
}
