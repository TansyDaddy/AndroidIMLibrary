package com.renyu.nimavchatlibrary.manager;

import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.module.AVChatTimeoutObserver;
import com.renyu.nimavchatlibrary.module.SimpleAVChatStateObserver;
import com.renyu.nimavchatlibrary.params.AVChatConfigs;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.util.AVChatSoundPlayer;
import com.renyu.nimavchatlibrary.util.RxBusWithAV;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseAVManager {

    // 当前音频状态
    AVChatTypeListener avChatTypeListener;
    public interface AVChatTypeListener {
        void chatTypeChange(AVChatTypeEnum avChatTypeEnum);
    }

    // 当前静音状态
    private AVChatMuteListener avChatMuteListener;
    public interface AVChatMuteListener {
        void chatMuteChange(boolean mute);
    }

    // 接收方音频通话信息
    public static AVChatData avChatData = null;
    // 是否音频通话连接成功
    public static AtomicBoolean isCallEstablish = new AtomicBoolean(false);

    private AVChatCameraCapturer mVideoCapturer;
    // 音频配置参数
    private AVChatConfigs avChatConfigs;
    // 是否已经结束音频服务
    private boolean destroyRTC = false;
    // 是否恢复音频通话
    private boolean needRestoreLocalAudio = false;

    BaseAVManager() {
        avChatConfigs = new AVChatConfigs(Utils.getApp());
    }

    /**
     * 参数需要重置
     */
    public void reSetParams() {
        destroyRTC = false;
        needRestoreLocalAudio = false;

        // 打开音频
        AVChatManager.getInstance().muteLocalAudio(false);
        if (avChatMuteListener != null) {
            avChatMuteListener.chatMuteChange(false);
        }
    }

    public void setAvChatTypeListener(AVChatTypeListener avChatTypeListener) {
        this.avChatTypeListener = avChatTypeListener;
    }

    public void setAVChatMuteListener(AVChatMuteListener avChatMuteListener) {
        this.avChatMuteListener = avChatMuteListener;
    }

    // 来去电未连通超时
    Observer<Integer> timeoutObserver = (Observer<Integer>) integer -> {
        Log.d("NIM_AV_APP", "timeoutObserver");
        // 挂断
        hangUp(AVChatExitCode.CANCEL);
        if (avChatTypeListener != null) {
            avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_NO_RESPONSE);
        }
        sendAvChatType(AVChatTypeEnum.PEER_NO_RESPONSE);
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
                if (avChatTypeListener != null) {
                    avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONN);
                }
            } else {
                avChatData = null;
                // 注销未连通超时
                AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);
                if (code == 101) {
                    // 连接超时
                    Log.d("NIM_AV_APP", "onJoinedChannel 连接超时");
                    if (avChatTypeListener != null) {
                        avChatTypeListener.chatTypeChange(AVChatTypeEnum.PEER_NO_RESPONSE);
                    }
                    sendAvChatType(AVChatTypeEnum.PEER_NO_RESPONSE);
                    showQuitToast(AVChatExitCode.PEER_NO_RESPONSE);
                } else if (code == 401) {
                    // 验证失败
                    Log.d("NIM_AV_APP", "onJoinedChannel 验证失败");
                    if (avChatTypeListener != null) {
                        avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONFIG_ERROR);
                    }
                    sendAvChatType(AVChatTypeEnum.CONFIG_ERROR);
                    showQuitToast(AVChatExitCode.CONFIG_ERROR);
                } else if (code == 417) {
                    // 无效的channelId
                    Log.d("NIM_AV_APP", "onJoinedChannel 无效的channelId");
                    if (avChatTypeListener != null) {
                        avChatTypeListener.chatTypeChange(AVChatTypeEnum.INVALIDE_CHANNELID);
                    }
                    sendAvChatType(AVChatTypeEnum.INVALIDE_CHANNELID);
                    showQuitToast(AVChatExitCode.INVALIDE_CHANNELID);
                } else {
                    // 连接服务器错误，直接退出
                    Log.d("NIM_AV_APP", "onJoinedChannel 连接服务器错误，直接退出");
                    if (avChatTypeListener != null) {
                        avChatTypeListener.chatTypeChange(AVChatTypeEnum.CONFIG_ERROR);
                    }
                    sendAvChatType(AVChatTypeEnum.CONFIG_ERROR);
                    showQuitToast(AVChatExitCode.CONFIG_ERROR);
                }
            }
        }

        /**
         * 会话成功建立
         */
        @Override
        public void onCallEstablished() {
            super.onCallEstablished();
            Log.d("NIM_AV_APP", "onCallEstablished");
            // 注销未连通超时
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);
            // 音频通话建立
            isCallEstablish.set(true);
            // 开启扬声器
            AVChatManager.getInstance().setSpeaker(true);
            if (avChatTypeListener != null) {
                avChatTypeListener.chatTypeChange(AVChatTypeEnum.CALLEE_ACK_AGREE);
            }
        }
    };

    // 在线状态变化观察者
    Observer<StatusCode> onlineStatusObserver = (Observer<StatusCode>) statusCode -> {
        if (statusCode.wontAutoLogin()) {
            // 取消
            hangUp(AVChatExitCode.CANCEL);
            // 注销未连通超时
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);
        }
    };

    public void registerCommonObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avchatStateObserver, register);
    }

    private void initAVChatParams() {
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        }
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
    }

    /**
     * 主叫拨号
     * @param account
     * @param extendMessage
     */
    public void call(String account, String extendMessage) {
        initAVChatParams();
        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
        // 添加自定义参数
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = extendMessage;
        // 去电
        AVChatManager.getInstance().call2(account, AVChatType.AUDIO, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                // 去电成功
                BaseAVManager.avChatData = avChatData;
                // 注册未连通超时
                AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, true);
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
        initAVChatParams();
        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(Utils.getApp(), "本地音频启动失败", Toast.LENGTH_SHORT).show();
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

    /**
     * 主动挂断
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
                    // 在未接听的时候，注销未连通超时
                    AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
        stop();
        showQuitToast(type);
    }

    /**
     * 被动挂断
     * @param type
     */
    void onHangUp(int type) {
        closeRtc();
        showQuitToast(type);
    }

    private void closeRtc() {
        if (destroyRTC) {
            return;
        }
        stop();
    }

    private void stop() {
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        AVChatSoundPlayer.instance().stop();

        isCallEstablish.set(false);
        avChatData = null;
    }

    /**
     * 恢复语音发送
     */
    public void resumeVideo() {
        if (needRestoreLocalAudio) {
            AVChatManager.getInstance().muteLocalAudio(false);
            if (avChatMuteListener != null) {
                avChatMuteListener.chatMuteChange(false);
            }
            needRestoreLocalAudio = false;
        }
    }

    /**
     * 关闭语音发送
     */
    public void pauseVideo() {
        if (!AVChatManager.getInstance().isLocalAudioMuted()) {
            AVChatManager.getInstance().muteLocalAudio(true);
            if (avChatMuteListener != null) {
                avChatMuteListener.chatMuteChange(true);
            }
            needRestoreLocalAudio = true;
        }
    }

    /**
     * 切换音频开关
     */
    public void toggleMute() {
        // isMute是否处于静音状态
        if (!AVChatManager.getInstance().isLocalAudioMuted()) {
            // 关闭音频
            AVChatManager.getInstance().muteLocalAudio(true);
            if (avChatMuteListener != null) {
                avChatMuteListener.chatMuteChange(true);
            }
        } else {
            // 打开音频
            AVChatManager.getInstance().muteLocalAudio(false);
            if (avChatMuteListener != null) {
                avChatMuteListener.chatMuteChange(false);
            }
        }
    }

    /**
     * 音频状态发送点
     * @param avChatTypeEnum
     */
    void sendAvChatType(AVChatTypeEnum avChatTypeEnum) {
        // 通知音频接听刷新
        RxBusWithAV.getDefault().post(avChatTypeEnum);
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
}
