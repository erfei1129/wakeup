package com.lskj.wakeup;

import android.app.Application;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.lskj.wakeup.biz.SpeechUtil;

/**
 * @author Ge Xiaodong
 * @time 2019/9/25 13:57
 * @description
 */
public class WakeUpApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 语音唤醒
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5db263d2");
        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        Setting.setShowLog(false);
        // 必须初始化
        SpeechUtil.init(this);
    }
}
