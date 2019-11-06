package com.lskj.wakeup.biz;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ge Xiaodong
 * @time 2019/9/27 11:32
 * @description
 */
public class SpeechContent {

    private static SpeechContent mInstance;
    // ------------------------------------------------命令词识别结果类型----------------------------------------
    public static final int TYPE_ALL = 0; // 闹钟响
    public static final int TYPE_DATE = 1;
    public static final int TYPE_WEATHER = 2;
    public static final int TYPE_LIMIT = 3;
    public static final int TYPE_TEMP = 4;
    // 匹配结果最低置信度(得分)
    public static final int RECOGNITION_SCORE = 30;

    // ------------------------------------------------命令关键字----------------------------------------
    // 日期
    public static final String[] matchDate = new String[]{"星期几", "周几", "几号", "什么日子", "日期", "多少号"};
    // 天气
    public static final String[] matchWeather = new String[]{"天气", "气温", "温度"};
    // 限号
    public static final String[] matchLimit = new String[]{"限行", "限号"};
    // 温湿度
    public static final String[] matchTemp = new String[]{"室内温度", "", "室内湿度", "室内温湿度", "环境"};

    // ------------------------------------------------唤醒+识别初始化内容----------------------------------------

    private List<String> speakContent = new ArrayList<>();

    // 设置显示状态0隐藏,1显示
    public static int[] displayState = new int[]{1, 1, 1, 1, 1};
    // 闹铃播报内容
    private List<String> ringSpeakList = new ArrayList<>();

    public static SpeechContent getInstance() {
        if (mInstance == null) {
            synchronized (SpeechUtil.class) {
                if (mInstance == null) {
                    mInstance = new SpeechContent();
                }
            }
        }
        return mInstance;
    }

    // 初始化一次
    private SpeechContent() {
        // 响铃播报内容
        for (int i = 0; i < 5; i++) {
            ringSpeakList.add("");
        }

        // 播报内容列表
        for (int i = 0; i < 5; i++) {
            speakContent.add("");
        }
    }

    // 设置播报日期
    public void setSpeakTextDate(String speakTextDate) {
        ringSpeakList.set(0, speakTextDate);
        setRingSpeakText();
        speakContent.set(1, speakTextDate);
    }

    // 设置播报生日
    public void setSpeakTextBirth(String speakTextBirth) {
        ringSpeakList.set(1, speakTextBirth);
        setRingSpeakText();
    }

    // 设置播报天气
    public void setSpeakTextWeather(String speakTextWeather) {
        ringSpeakList.set(2, speakTextWeather);
        setRingSpeakText();
        speakContent.set(2, speakTextWeather);
    }

    // 设置播报限号
    public void setSpeakTextLimit(String speakTextLimit) {
        ringSpeakList.set(3, speakTextLimit);
        setRingSpeakText();
        speakContent.set(3, speakTextLimit);
    }

    // 设置播报玩湿度
    public void setSpeakTextTemp(String speakTextTemp) {
        ringSpeakList.set(4, speakTextTemp);
        setRingSpeakText();
        speakContent.set(4, speakTextTemp);
    }

    // 设置生日是否显示 只能传0,1
    public void setBirthDisplayState(int flag) {
        displayState[1] = flag;
        setRingSpeakText();
    }

    // 设置天气是否显示 只能传0,1
    public void setWeatherDisplayState(int flag) {
        displayState[2] = flag;
        setRingSpeakText();
    }

    // 设置限号是否显示 只能传0,1
    public void setLimitDisplayState(int flag) {
        displayState[3] = flag;
        setRingSpeakText();
    }

    // 获取闹钟播报内容
    private void setRingSpeakText() {
        StringBuilder ringSpeakText = new StringBuilder();

        for (int i = 0; i < displayState.length; i++) {
            if (displayState[i] == 1) {
                ringSpeakText.append(ringSpeakList.get(i));
            }
        }

        speakContent.set(0, ringSpeakText.toString());
    }

    // 获取播报内容列表
    public List<String> getSpeakContentList() {
        return speakContent;
    }

}
