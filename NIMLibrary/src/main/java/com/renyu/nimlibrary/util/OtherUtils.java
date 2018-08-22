package com.renyu.nimlibrary.util;

import android.view.View;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.TimeUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OtherUtils {

    public static String getFriendlyTimeSpanByNow(long millis) {
        long now = System.currentTimeMillis();
        // 获取当天00:00
        long wee = (now / TimeConstants.DAY) * TimeConstants.DAY - 8 * TimeConstants.HOUR;
        if (millis >= wee+1000*3600*12) {
            return String.format("下午%tR", millis);
        } else if (millis >= wee) {
            return String.format("上午%tR", millis);
        } else if (millis >= wee - TimeConstants.DAY) {
            return String.format("昨天", millis);
        } else {
            if (isSameDate(now, millis)) {
                return TimeUtils.getChineseWeek(millis);
            }
            else {
                return TimeUtils.millis2String(millis, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));
            }
        }
    }

    public static String getFriendlyTimeSpanByNow2(long millis) {
        long now = System.currentTimeMillis();
        // 获取当天00:00
        long wee = (now / TimeConstants.DAY) * TimeConstants.DAY - 8 * TimeConstants.HOUR;
        if (millis >= wee+1000*3600*12) {
            return String.format("下午 %tR", millis);
        } else if (millis >= wee) {
            return String.format("上午 %tR", millis);
        } else if (millis >= wee - TimeConstants.DAY) {
            return String.format("昨天 %tR", millis);
        } else {
            if (isSameDate(now, millis)) {
                return String.format(TimeUtils.getChineseWeek(millis)+" %tR", millis);
            }
            else {
                if (isSameYear(now, millis)) {
                    return String.format(TimeUtils.millis2String(millis, new SimpleDateFormat("MM-dd", Locale.getDefault()))+" %tR", millis);
                }
                else {
                    return String.format(TimeUtils.millis2String(millis, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))+" %tR", millis);
                }
            }
        }
    }

    private static boolean isSameDate(long t1, long t2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(t1);
        cal2.setTimeInMillis(t2);
        int subYear = cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
        if(subYear == 0) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        else if(subYear==1 && cal2.get(Calendar.MONTH)==11) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        else if(subYear==-1 && cal1.get(Calendar.MONTH)==11) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        return false;
    }

    private static boolean isSameYear(long t1, long t2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(t1);
        cal2.setTimeInMillis(t2);
        int subYear = cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
        return subYear == 0;
    }

    public static long getSecondsByMilliseconds(long milliseconds) {
        return (long) new BigDecimal((float) milliseconds / (float) 1000).setScale(0,
                BigDecimal.ROUND_HALF_UP).intValue();
    }

    /**
     * 判断文件是不是有后缀
     * @param filename
     * @return
     */
    public static boolean hasExtentsion(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > -1) && (dot < (filename.length() - 1));
    }

    /**
     * 获取音视频播放时间
     * @param time
     * @return
     */
    public static String secToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else retStr = "" + i;
        return retStr;
    }

    public static BitmapDescriptor getBitmapDescriptor(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        return BitmapDescriptorFactory.fromView(view);
    }
}
