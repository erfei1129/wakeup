package com.lskj.wakeup.biz;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lskj.wakeup.R;
import com.lskj.wakeup.test.MediaPlayUtil;
import com.lskj.wakeup.util.SpeechUtil;
import com.lskj.wakeup.util.TimeUtil;

import java.util.Date;

public class SpeechActivity1 extends AppCompatActivity {
    private static final String TAG = "SpeechUtil";

    private Button mBtnStart;
    private Button mBtnPlay;
    private View mLlScroll, mLlAnimView;
    private View mLlDateGroup, mLlBirthdayGroup, mLlWeatherGroup, mLlLimitGroup, mLlTempHumidGroup;
    private TextView mTvDate;
    private TextView mTvBirthdayNotice;
    private ImageView mIvWeatherType;
    private TextView mTvTemperature;
    private TextView mTvWeatherName;
    private TextView mTvLimit1;
    private TextView mTvLimit2;

    private TextView mTvTempHumid;
    private View mLlRingClock;
    private TextView mTvClockTime;


    // 闹铃播报内容
    private String speakText0 = "今天是2019年11月5日星期二。";
    private String speakText1 = "生日提醒：今天是豆豆生日，祝豆豆生日快乐，开心每一天！";
    private String speakText2 = "今日天气晴，最高气温37℃，最低气温29℃。";
    private String speakText3 = "今日车牌尾号为0、5的车辆限行。";
    private String speakText4 = "当前室内温度25℃，室内空气相对湿度30%。";
    private Animation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        findView();
        modifySpeakText();
        SpeechUtil.getInstance().addVoiceResultListener(voiceResultListener);
        initClick();
    }

    // 更新日期内容
    @SuppressLint("SetTextI18n")
    private void modifySpeakText() {
        mTvDate.setText("2019年11月5日星期二");
        speakText0 = "今天是2019年9月26日星期四。";
        SpeechContent.getInstance().setSpeakTextDate(speakText0);

        mTvBirthdayNotice.setText("豆豆今天生日");
        speakText1 = "生日提醒：今天是豆豆生日，祝豆豆生日快乐，开心每一天！";
        SpeechContent.getInstance().setSpeakTextBirth(speakText1);

        mTvWeatherName.setText("小雨");
        mTvTemperature.setText("20~26℃");
        speakText2 = "今日天气小雨，最高气温26℃，最低气温20℃。";
        SpeechContent.getInstance().setSpeakTextWeather(speakText2);

        mTvLimit1.setText("3");
        mTvLimit2.setText("6");
        speakText3 = "今日车牌尾号为3、6的车辆限行。";
        SpeechContent.getInstance().setSpeakTextLimit(speakText3);

        mTvTempHumid.setText("23℃、35%");
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
            mLlScroll.setVisibility(View.GONE);
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
            MediaPlayUtil.startRawPlay(SpeechActivity1.this, R.raw.ring_jingdian_01);
        });
    }

    private void findView() {
        mBtnStart = findViewById(R.id.btn_start);
        mBtnPlay = findViewById(R.id.btn_play);
        mLlScroll = findViewById(R.id.ll_scroll);
        mLlAnimView = findViewById(R.id.ll_anim_view);


        mLlDateGroup = findViewById(R.id.ll_date_group);
        mTvDate = findViewById(R.id.tv_date);
        mLlBirthdayGroup = findViewById(R.id.ll_birthday_group);
        mTvBirthdayNotice = findViewById(R.id.tv_birthday_notice);
        mLlWeatherGroup = findViewById(R.id.ll_weather_group);
        mIvWeatherType = findViewById(R.id.iv_weather_type);
        mTvTemperature = findViewById(R.id.tv_temperature);
        mTvWeatherName = findViewById(R.id.tv_weather_name);
        mLlLimitGroup = findViewById(R.id.ll_limit_group);
        mTvLimit1 = findViewById(R.id.tv_limit_1);
        mTvLimit2 = findViewById(R.id.tv_limit_2);
        mLlTempHumidGroup = findViewById(R.id.ll_temp_humid_group);

        mTvTempHumid = findViewById(R.id.tv_temp_humid);
        mLlRingClock = findViewById(R.id.ll_ring_clock);
        mTvClockTime = findViewById(R.id.tv_clock_time);
    }

    private VoiceResultListener voiceResultListener = new VoiceResultListener() {
        @Override
        public void receiveRecognition(int type) {
            Log.d(TAG, "type = " + type);

            setAllHint();
            mLlRingClock.setVisibility(View.GONE);
            mLlScroll.setVisibility(View.VISIBLE);
            switch (type) {
                case SpeechContent.TYPE_ALL:
                    setAllDisplay();
                    break;
                case SpeechContent.TYPE_DATE:  // 日期

                    mLlDateGroup.setVisibility(View.VISIBLE);
                    break;
                case SpeechContent.TYPE_WEATHER:  // 天气

                    mLlWeatherGroup.setVisibility(View.VISIBLE);
                    break;
                case SpeechContent.TYPE_LIMIT:  // 限号

                    mLlLimitGroup.setVisibility(View.VISIBLE);
                    break;
                case SpeechContent.TYPE_TEMP:  // 温湿度

                    mLlTempHumidGroup.setVisibility(View.VISIBLE);
                    break;
            }

            startAnimation(mLlAnimView);
        }

        @Override
        public void speakComplete(int type) {
            translateAnimation.cancel();
            new Handler().postDelayed(() -> {
                mLlScroll.setVisibility(View.GONE);
                switch (type) {
                    case SpeechContent.TYPE_ALL:
                        setAllHint();
                        break;
                    case SpeechContent.TYPE_DATE:  // 日期
                        mLlDateGroup.setVisibility(View.GONE);
                        break;
                    case SpeechContent.TYPE_WEATHER:  // 天气
                        mLlWeatherGroup.setVisibility(View.GONE);
                        break;
                    case SpeechContent.TYPE_LIMIT:  // 限号
                        mLlLimitGroup.setVisibility(View.GONE);
                        break;
                    case SpeechContent.TYPE_TEMP:  // 温湿度
                        mLlTempHumidGroup.setVisibility(View.GONE);
                        break;
                }

            }, 5000);
        }
    };

    private void startAnimation(View mAnimView) {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = 1280;
        if (null != wm) {
            screenWidth = wm.getDefaultDisplay().getWidth();
        }

        mAnimView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int animViewWidth = mAnimView.getMeasuredWidth();
        Log.d("startAnimation", "animViewWidth = " + animViewWidth);

        //平移动画  从0,0,平移到100,100
        translateAnimation = new TranslateAnimation(screenWidth - 30, -animViewWidth + 30, 0, 0);
//        translateAnimation.setDuration((animViewWidth + screenWidth) / 50 * 1000); //动画持续的时间为15s
        translateAnimation.setDuration(15 * 1000); //动画持续的时间为15s
        translateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        translateAnimation.setRepeatMode(ValueAnimator.RESTART);
        mAnimView.setAnimation(translateAnimation); //给imageView添加的动画效果
        translateAnimation.setFillEnabled(false); //使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(false); //不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到原点
        translateAnimation.startNow();//动画开始执行 放在最后即可
    }

    // 按照设置显示或隐藏
    private void setAllDisplay() {
        mLlDateGroup.setVisibility(View.VISIBLE);
        mLlBirthdayGroup.setVisibility(SpeechContent.displayState[1] == 1 ? View.VISIBLE : View.GONE);
        mLlWeatherGroup.setVisibility(SpeechContent.displayState[2] == 1 ? View.VISIBLE : View.GONE);
        mLlLimitGroup.setVisibility(SpeechContent.displayState[3] == 1 ? View.VISIBLE : View.GONE);
        mLlTempHumidGroup.setVisibility(View.VISIBLE);
    }

    // 全部隐藏
    private void setAllHint() {
        mLlDateGroup.setVisibility(View.GONE);
        mLlBirthdayGroup.setVisibility(View.GONE);
        mLlWeatherGroup.setVisibility(View.GONE);
        mLlLimitGroup.setVisibility(View.GONE);
        mLlTempHumidGroup.setVisibility(View.GONE);
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
}
