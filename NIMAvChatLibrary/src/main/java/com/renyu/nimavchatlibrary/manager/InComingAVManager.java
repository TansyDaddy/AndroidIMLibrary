package com.renyu.nimavchatlibrary.manager;

import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.renyu.nimavchatlibrary.module.AVChatTimeoutObserver;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.receiver.IncomingCallReceiver;
import com.renyu.nimavchatlibrary.util.AVChatSoundPlayer;

public class InComingAVManager extends BaseAVManager {

    public static InComingAVManager inComingAVManager;

    public InComingAVManager() {
        super();
    }

    public void setAvChatTypeListener(AVChatTypeListener avChatTypeListener) {
        super.setAvChatTypeListener(avChatTypeListener);
    }

    /**
     * 注册被叫监听
     */
    public void registerInComingObserver() {
        // 注册网络来电
        AVChatManager.getInstance().observeIncomingCall((Observer<AVChatData>) avChatData -> {
            // 非电话场景并且未发生音视频连接的情况下可以接收呼叫
            if (IncomingCallReceiver.stateEnum != IncomingCallReceiver.PhoneCallStateEnum.IDLE
                    || isAVChatting
                    || AVChatManager.getInstance().getCurrentChatId() != 0) {
                // 给对方用户发送占线指令
                AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.BUSY, null);
                return;
            }
            // 有来电发生
            BaseAVManager.avChatData = avChatData;
            isAVChatting = true;
            // 重置参数
            reSetParams();
            // 注册来电未接超时
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, true);

            AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
            Toast.makeText(Utils.getApp(), avChatData.getAccount()+"要求进行VR带看", Toast.LENGTH_SHORT).show();
        }, true);

        // 注册网络通话对方挂断的通知
        AVChatManager.getInstance().observeHangUpNotification((Observer<AVChatCommonEvent>) avChatCommonEvent -> {
            Log.d("NIM_AV_APP", "收到对方挂断电话");
            onHangUp(AVChatExitCode.HANGUP);
            // 注销来电超时
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);
            if (avChatTypeListener != null) {
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_HANG_UP);
            }

            Toast.makeText(Utils.getApp(), avChatCommonEvent.getAccount()+"终止VR带看", Toast.LENGTH_SHORT).show();
        }, true);

        // 在线状态变化观察者
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatusObserver, true);
    }

    /**
     * 被叫接听
     */
    public void receive() {
        initParams();
        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(Utils.getApp(), "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Utils.getApp(), "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                hangUp(AVChatExitCode.CANCEL);
            }

            @Override
            public void onException(Throwable exception) {
                hangUp(AVChatExitCode.CANCEL);
            }
        });
        AVChatSoundPlayer.instance().stop();
    }
}
