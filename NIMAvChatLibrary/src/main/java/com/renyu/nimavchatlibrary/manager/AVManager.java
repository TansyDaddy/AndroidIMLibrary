package com.renyu.nimavchatlibrary.manager;

import android.content.Context;

import com.renyu.nimavchatlibrary.AVChatKit;
import com.renyu.nimavchatlibrary.config.AVChatOptions;
import com.renyu.nimavchatlibrary.ui.activity.AVChatActivity;

public class AVManager {

    /**
     * 音视频通话基础配置
     */
    public static void init() {
        AVChatOptions avChatOptions = new AVChatOptions(){
            @Override
            public void logout(Context context) {

            }
        };
        AVChatKit.init(avChatOptions);
    }


    /**
     * 发起音视频通话呼叫
     * @param context   上下文
     * @param account   被叫方账号
     * @param displayName   被叫方显示名称
     * @param callType      音视频呼叫类型
     * @param source        发起呼叫的来源，参考AVChatActivityEx.FROM_INTERNAL/FROM_BROADCASTRECEIVER
     */
    public static void outgoingCall(Context context, String account, String displayName, int callType, int source) {
        AVChatActivity.outgoingCall(context, account, displayName, callType, source);
    }
}
