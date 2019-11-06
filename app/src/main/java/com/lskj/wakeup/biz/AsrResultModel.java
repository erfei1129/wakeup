package com.lskj.wakeup.biz;

import java.io.Serializable;

/**
 * @author Ge Xiaodong
 * @time 2019/9/25 15:41
 * @description
 */
public class AsrResultModel implements Serializable {

    // 识别结果
    private String resultText;
    // 置信度
    private int resultScore;

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public int getResultScore() {
        return resultScore;
    }

    public void setResultScore(int resultScore) {
        this.resultScore = resultScore;
    }
}
