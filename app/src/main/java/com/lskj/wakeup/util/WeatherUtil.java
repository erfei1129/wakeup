package com.lskj.wakeup.util;


import com.lskj.wakeup.R;
import com.lskj.wakeup.biz.SpeechContent;

/**
 * @author Ge Xiaodong
 * @time 2019/8/19 16:04
 * @description
 */
public class WeatherUtil {

    /**
     * 通过天气type找到icon的resId
     *
     * @param weatherType
     * @return
     */
    public static int getDayWeatherIconFromType(int weatherType) {
        switch (weatherType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                return SpeechContent.dayWeatherDrawables[weatherType];
            case 53:
                return SpeechContent.dayWeatherDrawables[32];
            case 301:
                return SpeechContent.dayWeatherDrawables[33];
            case 302:
                return SpeechContent.dayWeatherDrawables[34];
            default:
                return R.drawable.icon_weather_undefined;

        }
    }

    /**
     * 通过天气type找到icon的resId
     *
     * @param weatherType
     * @return
     */
    public static int getNightWeatherIconFromType(int weatherType) {
        switch (weatherType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                return SpeechContent.nightWeatherDrawables[weatherType];
            case 53:
                return SpeechContent.nightWeatherDrawables[32];
            case 301:
                return SpeechContent.nightWeatherDrawables[33];
            case 302:
                return SpeechContent.nightWeatherDrawables[34];
            default:
                return R.drawable.icon_weather_undefined;

        }
    }
}
