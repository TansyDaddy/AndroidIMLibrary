package com.renyu.nimavchatlibrary.noui.manager;

import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.config.AVChatConfigs;
import com.renyu.nimavchatlibrary.constant.AVChatExitCode;
import com.renyu.nimavchatlibrary.controll.AVChatSoundPlayer;
import com.renyu.nimavchatlibrary.module.AVChatTimeoutObserver;
import com.renyu.nimavchatlibrary.module.SimpleAVChatStateObserver;
import com.renyu.nimavchatlibrary.noui.AVChatActivity;
import com.renyu.nimavchatlibrary.noui.params.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.receiver.IncomingCallReceiver;

import java.util.concurrent.atomic.AtomicBoolean;

public class AVManager {

    // 是不是被叫
    private boolean mIsInComingCall;
    // 音视频通话信息
    private AVChatData avChatData;
    private AVChatCameraCapturer mVideoCapturer;
    // 音视频配置参数
    private AVChatConfigs avChatConfigs;
    // 是否已经结束音视频服务
    private boolean destroyRTC = false;
    // 是否恢复音频通话
    private boolean needRestoreLocalAudio = false;
    // 是否音视频通话连接成功
    private AtomicBoolean isCallEstablish = new AtomicBoolean(false);
    // 当前音视频状态
    private AVChatTypeListener avChatTypeListener;
    public interface AVChatTypeListener {
        void chatTypeChange(AVChatTypeEnum avChatTypeEnum);
    }

    private AVManager() {

    }

    public AVManager(AVChatData avChatData, boolean mIsInComingCall, AVChatTypeListener avChatTypeListener) {
        this.avChatData = avChatData;
        this.mIsInComingCall = mIsInComingCall;
        this.avChatTypeListener = avChatTypeListener;
        // 配置音视频参数
        avChatConfigs = new AVChatConfigs(Utils.getApp());
    }

    // 来去电超时
    private Observer<Integer> timeoutObserver = (Observer<Integer>) integer -> {
        Log.d("NIM_AV_APP", "timeoutObserver");
        hangUp(AVChatExitCode.CANCEL);
        avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_NO_RESPONSE);
    };

    // 网络通话控制命令通知
    private Observer<AVChatControlEvent> callControlObserver = (Observer<AVChatControlEvent>) netCallControlNotification -> {
        // 不是当前音视频聊天用户不接收其指令
        if (AVChatManager.getInstance().getCurrentChatId() != netCallControlNotification.getChatId()) {
            return;
        }
        Log.d("NIM_AV_APP", "音视频对方发来指令值：" + netCallControlNotification.getControlCommand());
    };

    private SimpleAVChatStateObserver avchatStateObserver = new SimpleAVChatStateObserver() {
        /**
         * 服务器连接回调
         * @param code
         * @param audioFile
         * @param videoFile
         * @param elapsed
         */
        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile, int elapsed) {
            super.onJoinedChannel(code, audioFile, videoFile, elapsed);
            Log.d("NIM_AV_APP", "onJoinedChannel 当前状态code："+code);
            if (code == 200) {
                Log.d("NIM_AV_APP", "onJoinedChannel 连接成功");
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONN);
            } else if (code == 101) {
                // 连接超时
                Log.d("NIM_AV_APP", "onJoinedChannel 连接超时");
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_NO_RESPONSE);
                showQuitToast(AVChatExitCode.PEER_NO_RESPONSE);
            } else if (code == 401) {
                // 验证失败
                Log.d("NIM_AV_APP", "onJoinedChannel 验证失败");
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONFIG_ERROR);
                showQuitToast(AVChatExitCode.CONFIG_ERROR);
            } else if (code == 417) {
                // 无效的channelId
                Log.d("NIM_AV_APP", "onJoinedChannel 无效的channelId");
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.INVALIDE_CHANNELID);
                showQuitToast(AVChatExitCode.INVALIDE_CHANNELID);
            } else {
                // 连接服务器错误，直接退出
                Log.d("NIM_AV_APP", "onJoinedChannel 连接服务器错误，直接退出");
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONFIG_ERROR);
                showQuitToast(AVChatExitCode.CONFIG_ERROR);
            }
        }

        /**
         * 会话成功建立
         */
        @Override
        public void onCallEstablished() {
            super.onCallEstablished();
            Log.d("NIM_AV_APP", "onCallEstablished");
            avChatTypeListener.chatTypeChange(AVChatTypeEnum.CALLEE_ACK_AGREE);
            //移除超时监听
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false, mIsInComingCall);
            // 开启扬声器
            AVChatManager.getInstance().setSpeaker(true);
        }
    };

    // 通话过程中，收到对方挂断电话
    private Observer<AVChatCommonEvent> callHangupObserver = (Observer<AVChatCommonEvent>) avChatCommonEvent -> {
        if (avChatData != null && avChatData.getChatId() == avChatCommonEvent.getChatId()) {
            Log.d("NIM_AV_APP", "收到对方挂断电话");
            onHangUp(AVChatExitCode.HANGUP);
            avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_HANG_UP);
        }
    };

    // 呼叫时，被叫方的响应（接听、拒绝、忙）
    private Observer<AVChatCalleeAckEvent> callAckObserver = (Observer<AVChatCalleeAckEvent>) avChatCalleeAckEvent -> {
        if (avChatData != null && avChatData.getChatId() == avChatCalleeAckEvent.getChatId()) {
            AVChatSoundPlayer.instance().stop();
            if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                Log.d("NIM_AV_APP", "被叫方正在忙");
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
                onHangUp(AVChatExitCode.PEER_BUSY);
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CALLEE_ACK_BUSY);
            } else if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                Log.d("NIM_AV_APP", "被叫方拒绝通话");
                onHangUp(AVChatExitCode.REJECT);
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CALLEE_ACK_REJECT);
            } else if (avChatCalleeAckEvent.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                Log.d("NIM_AV_APP", "被叫方同意通话");
                isCallEstablish.set(true);
            }
        }
    };

    // 注册/注销同时在线的其他端对主叫方的响应
    private Observer<AVChatOnlineAckEvent> onlineAckObserver = (Observer<AVChatOnlineAckEvent>) avChatOnlineAckEvent -> {
        Log.d("NIM_AV_APP", "AVChatOnlineAckEvent");
        if (avChatData != null && avChatData.getChatId() == avChatOnlineAckEvent.getChatId()) {
            AVChatSoundPlayer.instance().stop();

            String client = null;
            switch (avChatOnlineAckEvent.getClientType()) {
                case ClientType.Web:
                    client = "Web";
                    break;
                case ClientType.Windows:
                    client = "Windows";
                    break;
                case ClientType.Android:
                    client = "Android";
                    break;
                case ClientType.iOS:
                    client = "iOS";
                    break;
                case ClientType.MAC:
                    client = "Mac";
                    break;
                default:
                    break;
            }
            if (client != null) {
                String option = avChatOnlineAckEvent.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
                Toast.makeText(Utils.getApp(), "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void registerObserves(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avchatStateObserver, register);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, register, mIsInComingCall);
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
    }

    /**
     * 主叫拨号
     * @param account
     * @param extendMessage
     */
    public void call(String account, String extendMessage) {
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        // 添加自定义参数
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = extendMessage;
        // 去电
        AVChatManager.getInstance().call2(account, AVChatType.AUDIO, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                // 去电成功
                AVManager.this.avChatData = avChatData;
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_FORBIDDEN) {
                    Toast.makeText(Utils.getApp(), R.string.avchat_no_permission, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Utils.getApp(), R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                }
                closeRtc();
            }

            @Override
            public void onException(Throwable exception) {
                closeRtc();
            }
        });
    }

    /**
     * 被叫接听
     */
    public void receive() {
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        }
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isCallEstablish.set(true);
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
    }

    /**
     * 挂断
     * @param type
     */
    public void hangUp(int type) {
        if (destroyRTC) {
            return;
        }
        if ((type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE
                || type == AVChatExitCode.CANCEL || type == AVChatExitCode.REJECT) && avChatData != null) {
            AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        AVChatSoundPlayer.instance().stop();
        showQuitToast(type);
    }

    /**
     * 挂断
     * @param exitCode
     */
    private void onHangUp(int exitCode) {
        if (destroyRTC) {
            return;
        }
        AVChatSoundPlayer.instance().stop();
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        showQuitToast(exitCode);
    }

    private void closeRtc() {
        if (destroyRTC) {
            return;
        }
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        AVChatSoundPlayer.instance().stop();
    }

    private void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(Utils.getApp(), R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.REJECT:
                Toast.makeText(Utils.getApp(), R.string.avchat_call_reject, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_HANGUP:
            case AVChatExitCode.HANGUP:
                if (isCallEstablish.get()) {
                    Toast.makeText(Utils.getApp(), R.string.avchat_call_finish, Toast.LENGTH_SHORT).show();
                }
                break;
            case AVChatExitCode.PEER_BUSY:
                Toast.makeText(Utils.getApp(), R.string.avchat_peer_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_PEER_LOWER:
                Toast.makeText(Utils.getApp(), R.string.avchat_peer_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_SELF_LOWER:
                Toast.makeText(Utils.getApp(), R.string.avchat_local_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.INVALIDE_CHANNELID:
                Toast.makeText(Utils.getApp(), R.string.avchat_invalid_channel_id, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.LOCAL_CALL_BUSY:
                Toast.makeText(Utils.getApp(), R.string.avchat_local_call_busy, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 恢复语音发送
     */
    public void resumeVideo() {
        if (needRestoreLocalAudio) {
            AVChatManager.getInstance().muteLocalAudio(false);
            needRestoreLocalAudio = false;
        }

    }

    /**
     * 关闭语音发送
     */
    public void pauseVideo() {
        if (!AVChatManager.getInstance().isLocalAudioMuted()) {
            AVChatManager.getInstance().muteLocalAudio(true);
            needRestoreLocalAudio = true;
        }
    }

    /**
     * 注册网络来电
     */
    public static void observeIncomingCall() {
        AVChatManager.getInstance().observeIncomingCall((Observer<AVChatData>) avChatData -> {
            String extra = avChatData.getExtra();
            if (IncomingCallReceiver.stateEnum != IncomingCallReceiver.PhoneCallStateEnum.IDLE
                    || AVManager.isAVChatting
                    || AVChatManager.getInstance().getCurrentChatId() != 0) {
                // 给对方用户发送占线指令
                AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.BUSY, null);
                return;
            }
            // 有网络来电打开AVChatActivity
            AVChatActivity.incomingCall(Utils.getApp(), avChatData.getAccount(), extra, AVChatType.AUDIO.getValue(), avChatData, AVChatActivity.FROM_BROADCASTRECEIVER);
        }, true);
    }

    // 是否进入音视频通话
    public static boolean isAVChatting = false;

    public AVChatData getAvChatData() {
        return avChatData;
    }

    public AtomicBoolean getIsCallEstablish() {
        return isCallEstablish;
    }
}
