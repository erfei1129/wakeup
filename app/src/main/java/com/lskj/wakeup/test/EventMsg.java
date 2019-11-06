package com.lskj.wakeup.test;

import java.io.Serializable;

/**
 * @author Ge Xiaodong
 * @time 2019/9/25 18:29
 * @description
 */
public class EventMsg implements Serializable {
    private int cmdType;

    public EventMsg() {
    }

    public EventMsg(int cmdType) {
        this.cmdType = cmdType;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }
}
