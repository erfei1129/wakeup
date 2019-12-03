package com.lskj.wakeup.biz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.lskj.wakeup.test.MediaPlayUtil;
import com.lskj.wakeup.R;
import com.lskj.wakeup.util.SpeechUtil;
import com.lskj.wakeup.util.TimeUtil;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechActivity extends AppCompatActivity {
    private static final String TAG = "SpeechUtil";

    private Button mBtnStart;
    private Button mBtnPlay;

    private View mLlRingClock;
    private TextView mTvClockTime;
    private TextView mTvContent;

    // mTvContent要显示的内容
    private String dateText, birthText, weatherText, limitText, tempHumidText;
    // 闹铃播报内容
    private String speakText0 = "今天是2019年11月5日星期二。";
    private String speakText1 = "生日提醒：今天是豆豆生日，祝豆豆生日快乐，开心每一天！";
    private String speakText2 = "今日天气晴，最高气温37℃，最低气温29℃。";
    private String speakText3 = "今日车牌尾号为0、5的车辆限行。";
    private String speakText4 = "当前室内温度25℃，室内空气相对湿度30%。";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        modifySpeakText();
        SpeechUtil.getInstance().addVoiceResultListener(voiceResultListener);
        initClick();
    }

    private void modifySpeakText() {
        dateText = "2019年11月5日星期二";
        speakText0 = "今天是2019年9月26日星期四。";
        SpeechContent.getInstance().setSpeakTextDate(speakText0);

        birthText = "豆豆今天生日";
        speakText1 = "生日提醒：今天是豆豆生日，祝豆豆生日快乐，开心每一天！";
        SpeechContent.getInstance().setSpeakTextBirth(speakText1);

        weatherText = "小雨  20~26℃";
        speakText2 = "今日天气小雨，最高气温26℃，最低气温20℃。";
        SpeechContent.getInstance().setSpeakTextWeather(speakText2);

        limitText = "3  6";
        speakText3 = "今日车牌尾号为3、6的车辆限行。";
        SpeechContent.getInstance().setSpeakTextLimit(speakText3);

        tempHumidText = "23℃、35%";
        speakText4 = "当前室内温度23℃，室内空气相对湿度35%。";
        SpeechContent.getInstance().setSpeakTextTemp(speakText4);
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void initClick() {
        mBtnStart.setOnClickListener(v -> startWakeup());

        mBtnPlay.setOnClickListener(v -> {
            mTvClockTime.setText(TimeUtil.getDateStr(new Date(), "当前时间 HH:mm"));
            // 模拟闹钟响了,闹钟开始
            // 显示闹钟图标
            mTvContent.setVisibility(View.GONE);
            mLlRingClock.setVisibility(View.VISIBLE);
            // 1.关闭播放,2.关闭监听,
            SpeechUtil.getInstance().stopWakeuper();
            SpeechUtil.getInstance().stopSpeak();
            // 响铃结束监听
            MediaPlayUtil.setRawCompleteListener(() -> {
                // 隐藏闹钟图标
                mLlRingClock.setVisibility(View.GONE);
                // 3.播放全部内容
                SpeechUtil.getInstance().startMixSpeech(SpeechContent.TYPE_ALL);
            });
            // 响铃
            MediaPlayUtil.startRawPlay(SpeechActivity.this, R.raw.ring_jingdian_01);
        });
    }

    private void findView() {
        mBtnStart = findViewById(R.id.btn_start);
        mBtnPlay = findViewById(R.id.btn_play);

        mTvContent = findViewById(R.id.tv_content);

        mLlRingClock = findViewById(R.id.ll_ring_clock);
        mTvClockTime = findViewById(R.id.tv_clock_time);
    }

    private VoiceResultListener voiceResultListener = new VoiceResultListener() {
        @Override
        public void receiveRecognition(int type) {
            Log.d(TAG, "type = " + type);

//            setAllHint();
            mLlRingClock.setVisibility(View.GONE);
            mTvContent.setVisibility(View.VISIBLE);
            switch (type) {
                case SpeechContent.TYPE_ALL:
                    setAllDisplay();
                    break;
                case SpeechContent.TYPE_DATE:  // 日期
                    setDateDisplay();
                    break;
                case SpeechContent.TYPE_WEATHER:  // 天气
                    setWeatherDisplay();
                    break;
                case SpeechContent.TYPE_LIMIT:  // 限号
                    setLimitDisplay();
                    break;
                case SpeechContent.TYPE_TEMP:  // 温湿度
                    setTempHumidDisplay();
                    break;
            }

//            startAnimation(mTvContent);
        }

        @Override
        public void speakComplete(int type) {
            new Handler().postDelayed(() -> {
                mTvContent.setVisibility(View.GONE);
            }, 5000);
        }
    };

    // 显示温湿度
    private void setTempHumidDisplay() {
        String content = "  <img>3_3<img>  " + tempHumidText;
        setDrawableText(content);
    }

    // 显示限号
    private void setLimitDisplay() {
        String content = "  <img>3_2<img>  " + limitText;
        setDrawableText(content);
    }

    // 显示天气
    private void setWeatherDisplay() {
        int dayNight = 2;
        int weatherType = 3;
        String content = "  <img>" + dayNight + "_" + weatherType + "<img>  " + weatherText;
        setDrawableText(content);
    }

    // 显示日期
    private void setDateDisplay() {
        String content = "  <img>3_0<img>  " + dateText;
        setDrawableText(content);
    }

    // 按照设置显示或隐藏
    private void setAllDisplay() {
        // 修改显示内容
        // 组装数据
        int dayNight = 1;
        int weatherType = 0;
        String content = "  <img>3_0<img>  " + dateText + "  <img>3_1<img>  " + birthText + "  <img>" + dayNight + "_" + weatherType + "<img>  " + weatherText + "  <img>3_2<img>  " + limitText + "  <img>3_3<img>  " + tempHumidText + "  ";
        setDrawableText(content);
    }

    /**
     * 设置图片和文字
     *
     * @param content
     * @return
     */
    private void setDrawableText(String content) {
        SpannableStringBuilder contentSpannable = new SpannableStringBuilder(content);
        Pattern pattern = Pattern.compile("<img>[^<>]+<img>");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            Log.d(TAG, matcher.group());
            String group = matcher.group();
            final int start = matcher.start();
            final int end = matcher.end();
            //网络图片
            //获取图片url（去掉'['和']'）
            String url = group.substring(5, group.length() - 5);
            if (TextUtils.isEmpty(url)) {
                return;
            }

            int drawableRes = 0;

            switch (Integer.parseInt(url.split("_")[0])) {
                case 1:
                    drawableRes = SpeechContent.dayWeatherDrawables[Integer.parseInt(url.split("_")[1])];
                    break;
                case 2:
                    drawableRes = SpeechContent.nightWeatherDrawables[Integer.parseInt(url.split("_")[1])];
                    break;
                case 3:
                    drawableRes = SpeechContent.contentTypeDrawables[Integer.parseInt(url.split("_")[1])];
                    break;
            }

            Glide.with(SpeechActivity.this)
                    .load(drawableRes)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            //获取图片宽高比
                            int bitmapWidth = resource.getIntrinsicWidth();
                            int bitmapHeight = resource.getIntrinsicHeight();

                            float ratio = bitmapWidth * 1.0f / bitmapHeight;

                            if (bitmapWidth < dp2px(15)) {
                                resource.setBounds(0, 0, dp2px(15), (int) (dp2px(15) / ratio));
                            } else if (bitmapWidth > dp2px(15)) {
                                resource.setBounds(0, 0, dp2px(15), (int) (dp2px(15) / ratio));
                            } else {
                                resource.setBounds(0, 0, bitmapWidth, bitmapHeight);
                            }
                            ImageSpan imageSpan = new ImageSpan(resource);
                            contentSpannable.setSpan(imageSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            mTvContent.setText(contentSpannable);
                        }
                    });
        }
        mTvContent.setText(contentSpannable);
        mTvContent.setSelected(true);
    }

    private void startWakeup() {
        SpeechUtil.getInstance().startWakeuper();
    }

    @Override
    protected void onDestroy() {
        SpeechUtil.getInstance().removeVoicceResultListener(voiceResultListener);

        SpeechUtil.getInstance().destroyWakeuper();
        SpeechUtil.getInstance().destroySpeechMix();

        super.onDestroy();
    }


    /**
     * dp转px
     *
     * @param dpVal dp device_admin
     * @return px device_admin
     */
    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                getResources().getDisplayMetrics());
    }
}
