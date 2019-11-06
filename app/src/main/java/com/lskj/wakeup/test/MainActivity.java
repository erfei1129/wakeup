package com.lskj.wakeup.test;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.lskj.wakeup.R;
import com.lskj.wakeup.biz.SpeechActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "mIflytekWakeUp";

    //    private IflytekWakeUp mIflytekWakeUp;
    private WakeUpAndRecognitionUtil mIflytekWakeUp;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private String voicerLocal = "xiaoyan";
    private String[] voiceText = new String[]{"", "今日天气晴，最高气温37℃，最低气温29℃。", "今天是2019年9月9日星期一。",
            "今日车牌尾号为0、5的车辆限行。", "当前室内温度25℃，室内空气相对湿度30%。"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

//        findViewById(R.id.btn_start).setOnClickListener(v -> startWakeup());
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SpeechActivity.class));
            MainActivity.this.finish();
        });

//        mIflytekWakeUp = new IflytekWakeUp(MainActivity.this);
//        mIflytekWakeUp = new WakeUpAndRecognitionUtil(MainActivity.this);
//        // 初始化合成对象
//        mTts = SpeechSynthesizer.createSynthesizer(this, code -> {
//            Log.d(TAG, "InitListener init() code = " + code);
//            if (code != ErrorCode.SUCCESS) {
//                Log.e(TAG, "初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
//            } else {
//                // 初始化成功，之后可以调用startSpeaking方法
//                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
//                // 正确的做法是将onCreate中的startSpeaking调用移至这里
//            }
//        });
    }

    // 语音唤醒
    private void startWakeup() {
        mIflytekWakeUp.initAsrAndStartWakeuper();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMsg event) {
        switch (event.getCmdType()) {
            // 天气
            case WakeUpAndRecognitionUtil.TYPE_WEATHER:

                startMixAndPlay(WakeUpAndRecognitionUtil.TYPE_WEATHER);
                break;
            //日期
            case WakeUpAndRecognitionUtil.TYPE_DATE:
                startMixAndPlay(WakeUpAndRecognitionUtil.TYPE_DATE);
                break;

            // 限号
            case WakeUpAndRecognitionUtil.TYPE_LIMIT:
                startMixAndPlay(WakeUpAndRecognitionUtil.TYPE_LIMIT);
                break;
            // 温湿度
            case WakeUpAndRecognitionUtil.TYPE_TEMP:
                startMixAndPlay(WakeUpAndRecognitionUtil.TYPE_TEMP);
                break;
        }
    }

    private void startMixAndPlay(int type) {
        int code = mTts.startSpeaking(voiceText[type], mTtsListener);
        // 设置参数
        setParam();
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG, "语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.d(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.d(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.d(TAG, "继续播放");
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
            } else if (error != null) {
                Log.e(TAG, error.getPlainDescription(true));
            }

            mIflytekWakeUp.startWakeuper();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}

            //实时音频流输出参考
			/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
				byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
				Log.e("MscSpeechLog", "buf is =" + buf);
			}*/
        }
    };

    /**
     * 参数设置
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);
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
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + voicerLocal + ".jet"));
        return tempBuffer.toString();
    }

//    @Override
//    protected void onDestroy() {
//        EventBus.getDefault().unregister(this);
//        mIflytekWakeUp.destroyWakeuper();
//
//        if (null != mTts) {
//            mTts.stopSpeaking();
//            // 退出时释放连接
//            mTts.destroy();
//        }
//
//        super.onDestroy();
//    }
}
