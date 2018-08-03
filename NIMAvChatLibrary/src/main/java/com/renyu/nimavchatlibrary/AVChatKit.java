package com.renyu.nimavchatlibrary;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.renyu.nimavchatlibrary.config.AVChatOptions;
import com.renyu.nimavchatlibrary.receiver.PhoneCallStateObserver;
import com.renyu.nimavchatlibrary.ui.activity.AVChatActivity;

/**
 * 云信音视频组件定制化入口
 * Created by winnie on 2017/12/6.
 */

public class AVChatKit {

    private static final String TAG = AVChatKit.class.getSimpleName();

    private static String account;


    private static AVChatOptions avChatOptions;

    public static void init(AVChatOptions avChatOptions) {
        AVChatKit.avChatOptions = avChatOptions;

        registerAVChatIncomingCallObserver(true);
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        AVChatKit.account = account;
    }

    /**
     * 获取音视频初始化配置
     * @return AVChatOptions
     */
    public static AVChatOptions getAvChatOptions() {
        return avChatOptions;
    }

    /**
     * 注册音视频来电观察者
     * @param register 注册或注销
     */
    private static void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(inComingCallObserver, register);
    }

    private static Observer<AVChatData> inComingCallObserver = (Observer<AVChatData>) data -> {
        if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
                || AVChatProfile.getInstance().isAVChatting()
                || AVChatManager.getInstance().getCurrentChatId() != 0) {
            AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);
            return;
        }
        // 有网络来电打开AVChatActivity
        AVChatProfile.getInstance().setAVChatting(true);
        AVChatProfile.getInstance().launchActivity(data, "测试", AVChatActivity.FROM_BROADCASTRECEIVER);
    };
}
