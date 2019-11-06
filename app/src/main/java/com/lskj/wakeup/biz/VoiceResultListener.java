package com.lskj.wakeup.biz;

/**
 * @author Ge Xiaodong
 * @time 2019/9/26 09:44
 * @description
 */
public interface VoiceResultListener {
    void receiveRecognition(int type);

    void speakComplete(int type);
}
