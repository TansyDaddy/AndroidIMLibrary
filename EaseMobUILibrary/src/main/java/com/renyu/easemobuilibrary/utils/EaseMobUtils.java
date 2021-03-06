package com.renyu.easemobuilibrary.utils;

import android.app.Application;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;
import com.renyu.easemobuilibrary.model.BroadcastBean;
import com.renyu.easemobuilibrary.params.CommonParams;

public class EaseMobUtils {

    /**
     * 配置环信基础选项
     * @return
     */
    public static void initChatOptions(Application application, String appKey){
        EMOptions options = new EMOptions();
        options.setAppKey(appKey);
        // 默认添加好友时，是不需要验证的，改成需要验证
//        options.setAcceptInvitationAlways(false);
        // 设置是否需要发送已读回执
        options.setRequireAck(true);
        // 设置是否需要接受方送达确认,默认false
        options.setRequireDeliveryAck(true);

        EMClient.getInstance().init(application, options);
    }

    /**
     * 监听连接状态
     */
    public static void registerMessageListener() {
        EMClient.getInstance().addConnectionListener(new EMConnectionListener() {
            @Override
            public void onConnected() {
                Log.d("EaseMobUtils", "addConnectionListener onConnected:");
            }

            @Override
            public void onDisconnected(int errorCode) {
                Log.d("EaseMobUtils", "addConnectionListener errorCode:" + errorCode);
                switch (errorCode) {
                    case EMError.USER_REMOVED:
                        // 账户被删除
                        break;
                    case EMError.USER_LOGIN_ANOTHER_DEVICE:
                        // 账户在另外一台设备登录
                        CommonParams.isKickout = true;

                        // 环信登出
                        EaseMobUtils.logout();

                        // 清除缓存内容
                        SPUtils.getInstance().remove(CommonParams.SP_UNAME);
                        SPUtils.getInstance().remove(CommonParams.SP_PWD);
                        Log.d("EaseMobUtils", "发生注销");

                        BroadcastBean.sendBroadcast(BroadcastBean.EaseMobCommand.Kickout);
                        break;
                    case EMError.USER_KICKED_BY_CHANGE_PASSWORD:
                        // 用户修改密码
                        break;
                    case EMError.USER_KICKED_BY_OTHER_DEVICE:
                        // 用户被其他设备踢掉
                        break;
                    default:
                        // 其他异常
                        break;
                }
            }
        });
    }

    /**
     * 登录
     * @param id
     * @param password
     */
    public static void login(String id, String password) {
        EMClient.getInstance().login(id, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                BroadcastBean.sendBroadcast(BroadcastBean.EaseMobCommand.LoginRsp);
            }

            @Override
            public void onError(int code, String error) {
                BroadcastBean.sendBroadcast(BroadcastBean.EaseMobCommand.LoginRspERROR);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    /**
     * 注册
     * 注册模式分两种，开放注册和授权注册。只有开放注册时，才可以客户端注册
     * 开放注册是为了测试使用，正式环境中不推荐使用该方式注册环信账号；
     * 授权注册的流程应该是您服务器通过环信提供的 REST API 注册，之后保存到您的服务器或返回给客户端。
     * @param id
     * @param password
     */
    public static void createAccount(String id, String password) {
        try {
            EMClient.getInstance().createAccount(id, password);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出登录
     */
    public static void logout() {
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }
}
