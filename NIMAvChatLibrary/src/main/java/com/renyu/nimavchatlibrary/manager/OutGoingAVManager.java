package com.renyu.nimavchatlibrary.manager;

import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.renyu.nimavchatlibrary.module.AVChatTimeoutObserver;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.util.AVChatSoundPlayer;

public class OutGoingAVManager extends BaseAVManager {

    // 呼叫时，被叫方的响应（接听、拒绝、忙）
    private Observer<AVChatCalleeAckEvent> callAckObserver = (Observer<AVChatCalleeAckEvent>) avChatCalleeAckEvent -> {
        if (avChatData != null && avChatData.getChatId() == avChatCalleeAckEvent.getChatId()) {
            if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                Log.d("NIM_AV_APP", "被叫方正在忙");
                if (avChatTypeListener != null) {
                    avChatTypeListener.chatTypeChange(AVChatTypeEnum.CALLEE_ACK_BUSY);
                }
            } else if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                Log.d("NIM_AV_APP", "被叫方拒绝通话");
            } else if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                Log.d("NIM_AV_APP", "被叫方同意通话");
                AVChatSoundPlayer.instance().stop();
            }
        }
    };

    private Observer<AVChatCommonEvent> callHangupObserver = (Observer<AVChatCommonEvent>) avChatCommonEvent -> {
        Log.d("NIM_AV_APP", "收到对方挂断电话");
        onHangUp(AVChatExitCode.HANGUP);
        if (avChatTypeListener != null) {
            avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_HANG_UP);
        }
        // 注销未连通超时
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);

        Toast.makeText(Utils.getApp(), avChatCommonEvent.getAccount()+"终止VR带看", Toast.LENGTH_SHORT).show();
    };

    public OutGoingAVManager() {
        super();
    }

    public void setAvChatTypeListener(AVChatTypeListener avChatTypeListener) {
        super.setAvChatTypeListener(avChatTypeListener);
    }

    public void setAVChatMuteListener(AVChatMuteListener avChatMuteListener) {
        super.setAVChatMuteListener(avChatMuteListener);
    }

    public void registerOutgoingObserver(boolean register) {
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
        // 注册网络通话对方挂断的通知
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
        // 监听踢下线通知
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatusObserver, register);
    }
}
