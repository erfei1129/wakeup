package com.lskj.wakeup.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ge Xiaodong
 * @time 2019/7/13 16:11
 * @description
 */
public class TimeUtil {

    /**
     * 判断是白天还是晚上
     *
     * @return
     */
    public static boolean isDayOrNight() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String hour = sdf.format(new Date());
        int k = Integer.parseInt(hour);
        return (k >= 0 && k < 6) || (k >= 18 && k < 24);
    }

    /**
     * 判断传过来的日期是不是今天
     *
     * @param date
     * @return
     */
    public static boolean dateIsToday(String date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        String todayDateStr = sdf.format(new Date());
        if (date.endsWith(todayDateStr))
            return true;
        else
            return false;
    }

    /**
     * 获取日期
     *
     * @return
     */
    public static String getDateStr(long time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDateStr = sdf.format(new Date(time));
        return todayDateStr;
    }

    /**
     * 获取日期+周几
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateStr(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("MM-ddEEEE");
        Date date;
        try {
            date = df.parse(dateStr);
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return sdf.format(new Date());
        }
    }

    /**
     * 获取当天最大时间
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateStr(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return sdf.format(new Date());
        }
    }

    /**
     * 获取日期Long
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static long getDateLong(String dateStr, String pattern) {
        if (TextUtils.isEmpty(dateStr)) return 0;
        DateFormat df;
        if (TextUtils.isEmpty(pattern)) {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            df = new SimpleDateFormat(pattern);
        }
        try {
            Date date = df.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取开始时间 (今天的最大时间-几天)
     *
     * @param displayDays
     * @return
     */
    public static long getTodayStartTime(int displayDays) {
        String timeStr = TimeUtil.getDateStr(new Date(), "yyyy-MM-dd 23:59:59");
        long timeLong = TimeUtil.getDateLong(timeStr, "");
        return timeLong - displayDays * 24 * 60 * 60 * 1000;
    }
}
