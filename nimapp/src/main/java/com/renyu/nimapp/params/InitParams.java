package com.renyu.nimapp.params;

import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.renyu.nimapp.R;

/**
 * 参数配置位置，因为通过反射，所以一定要类名位置写对
 */
public class InitParams {
    // 自定义的流程中控activity
    public static String InitActivityName = "com.renyu.nimapp.ui.activity.SplashActivity";
    // 自定义的会话详情activity
    public static String ConversationActivityName = "com.renyu.nimapp.ui.activity.ConversationActivity";
    // 通知栏图片
    public static int notificationIcon = R.mipmap.ic_launcher;
    // 通知栏颜色
    public static int notificationColor = R.color.colorPrimary;

    // 自定义的踢下线逻辑
    public static void kickoutFunc() {
        Toast.makeText(Utils.getApp(), "HI，我退出了", Toast.LENGTH_SHORT).show();
    }
}
