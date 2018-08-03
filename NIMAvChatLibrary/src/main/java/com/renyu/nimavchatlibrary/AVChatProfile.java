package com.renyu.nimavchatlibrary;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.renyu.nimavchatlibrary.common.Handlers;
import com.renyu.nimavchatlibrary.ui.activity.AVChatActivity;

/**
 * Created by huangjun on 2015/5/12.
 */
public class AVChatProfile {

    private boolean isAVChatting = false; // 是否正在音视频通话

    public static AVChatProfile getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isAVChatting() {
        return isAVChatting;
    }

    public void setAVChatting(boolean chating) {
        isAVChatting = chating;
    }

    private static class InstanceHolder {
        final static AVChatProfile instance = new AVChatProfile();
    }

    public void launchActivity(final AVChatData data, final String displayName, final int source) {
        Runnable runnable = () -> {
            // 启动，如果 task正在启动，则稍等一下
            // TODO: 2018/8/3 0003 需要判断一下
            AVChatActivity.incomingCall(Utils.getApp(), data, displayName, source);
        };
        Handlers.sharedHandler(Utils.getApp()).postDelayed(runnable, 200);
    }
}