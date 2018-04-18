package com.renyu.mt.params;

/**
 * 参数配置位置，因为通过反射，所以一定要类名位置写对
 */
public class InitParams {
    public static String VERSION = "1.00.01";
    public static String messageHeaderDomain = "FBS365";
    // fileprovider存储文件夹的名称
    public static String StorageName = "mt";
    // 自定义的流程中控activity
    public static String InitActivityName = "com.renyu.mt.activity.SplashActivity";
    // 自定义的会话详情activity
    public static String ConversationActivityName = "com.renyu.mt.activity.ConversationActivity";
}
