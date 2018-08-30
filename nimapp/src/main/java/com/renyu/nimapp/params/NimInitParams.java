package com.renyu.nimapp.params;

import com.renyu.nimapp.R;
import com.renyu.nimlibrary.bean.ObserveResponse;
import com.renyu.nimlibrary.bean.ObserveResponseType;
import com.renyu.nimlibrary.util.RxBus;

/**
 * 参数配置位置，因为通过反射，所以一定要类名位置写对
 */
public class NimInitParams {
    // 自定义的会话详情activity
    public static String ConversationActivityName = "com.renyu.nimapp.ui.activity.ConversationActivity";
    // 自定义的通知处理activity
    public static String NotificationActivityName = "com.renyu.nimapp.ui.activity.NotificationActivity";

    // 通知栏图片
    public static int notificationIcon = R.mipmap.ic_launcher;
    // 通知栏颜色
    public static int notificationColor = R.color.colorPrimary;

    // 小米推送参数配置
    public static String xmAppId = "2882303761517846609";
    public static String xmAppKey = "5801784697609";
    public static String xmCertificateName = "nimxiaomi";
    // 华为参数配置
    public static String hwCertificateName = "nimhuawei";

    // 是否为经纪人（区分租售宝和淘房）
    public static boolean isAgent = true;

    // 自定义的踢下线逻辑
    public static void kickoutFunc() {
        RxBus.getDefault().post(new ObserveResponse("", ObserveResponseType.Kickout));
    }

    // ***********************************  Demo配置使用，不是必须实现内容  ***********************************

    // app页面回收处理使用
    public static boolean isRestore = false;

    public static boolean isFirst = false;

    public static final String TYPE = "type";
    // 退出App
    public static final int FINISH = 1;
    // 被踢下线
    public static final int KICKOUT = 2;
    // 登录返回键返回
    public static final int SIGNINBACK = 3;
    // 去主页
    public static final int MAIN = 4;
}
