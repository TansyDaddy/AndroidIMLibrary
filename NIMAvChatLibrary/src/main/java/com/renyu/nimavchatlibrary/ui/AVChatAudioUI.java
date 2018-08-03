package com.renyu.nimavchatlibrary.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.constant.AVChatExitCode;
import com.renyu.nimavchatlibrary.constant.CallStateEnum;
import com.renyu.nimavchatlibrary.controll.AVChatController;
import com.renyu.nimavchatlibrary.module.AVChatControllerCallback;
import com.renyu.nimavchatlibrary.module.AVSwitchListener;
import com.renyu.nimavchatlibrary.ui.activity.AVChatActivity;
import com.renyu.nimavchatlibrary.ui.widgets.ToggleListener;
import com.renyu.nimavchatlibrary.ui.widgets.ToggleState;
import com.renyu.nimavchatlibrary.ui.widgets.ToggleView;

/**
 * 音频界面显示变化以及点击事件
 * Created by winnie on 2017/12/10.
 */

public class AVChatAudioUI implements View.OnClickListener, ToggleListener {
    private static final String TAG = AVChatAudioUI.class.getSimpleName();

    private Context context;

    // view
    private View rootView;
    private View switchVideo;
    private SimpleDraweeView headImg;
    private TextView nickNameTV;
    private Chronometer time;
    private TextView notifyTV;

    private View mute_speaker_hangup;
    private ToggleView muteToggle;
    private ToggleView speakerToggle;
    private View recordToggle;
    private Button recordToggleButton;
    private View hangup;

    private View refuse_receive;
    private TextView refuseTV;
    private TextView receiveTV;

    private View recordView;
    private View recordTip;
    private View recordWarning;

    // state
    private boolean init = false;
    private boolean isInSwitch = false;
    private boolean isEnabled = false;
    private boolean isRecordWarning = false;

    // data
    private String account;
    private String displayName;
    private AVChatController avChatController;
    private AVSwitchListener avSwitchListener;
    private CallStateEnum callingState;

    public AVChatAudioUI(Context context, View root, AVChatData avChatData, String displayName,
                         AVChatController avChatController, AVSwitchListener avSwitchListener) {
        this.context = context;
        this.rootView = root;
        this.displayName = displayName;
        this.avChatController = avChatController;
        this.avSwitchListener = avSwitchListener;
    }

    private void findViews() {
        if (init || rootView == null) {
            return;
        }
        switchVideo = rootView.findViewById(R.id.avchat_audio_switch_video);
        switchVideo.setOnClickListener(this);

        headImg = (SimpleDraweeView) rootView.findViewById(R.id.avchat_audio_head);
        nickNameTV = (TextView) rootView.findViewById(R.id.avchat_audio_nickname);
        time = (Chronometer) rootView.findViewById(R.id.avchat_audio_time);
        notifyTV = (TextView) rootView.findViewById(R.id.avchat_audio_notify);

        mute_speaker_hangup = rootView.findViewById(R.id.avchat_audio_mute_speaker_huangup);
        View mute = mute_speaker_hangup.findViewById(R.id.avchat_audio_mute);
        muteToggle = new ToggleView(mute, ToggleState.OFF, this);
        View speaker = mute_speaker_hangup.findViewById(R.id.avchat_audio_speaker);
        speakerToggle = new ToggleView(speaker, ToggleState.OFF, this);
        recordToggle = mute_speaker_hangup.findViewById(R.id.avchat_audio_record);
        recordToggleButton = (Button) mute_speaker_hangup.findViewById(R.id.avchat_audio_record_button);

        hangup = mute_speaker_hangup.findViewById(R.id.avchat_audio_hangup);
        hangup.setOnClickListener(this);
        recordToggle.setOnClickListener(this);
        recordToggle.setEnabled(false);

        refuse_receive = rootView.findViewById(R.id.avchat_audio_refuse_receive);
        refuseTV = (TextView) refuse_receive.findViewById(R.id.refuse);
        receiveTV = (TextView) refuse_receive.findViewById(R.id.receive);
        refuseTV.setOnClickListener(this);
        receiveTV.setOnClickListener(this);

        recordView = rootView.findViewById(R.id.avchat_record_layout);
        recordTip = rootView.findViewById(R.id.avchat_record_tip);
        recordWarning = rootView.findViewById(R.id.avchat_record_warning);

        init = true;
    }

    public void onDestroy() {
        if (time != null) {
            time.stop();
        }
    }

    /**
     * ************************ 音频主流程事件 ************************
     */

    public void showIncomingCall(AVChatData avChatData) {
        // 接听方的数据是AVChatData
        this.account = avChatData.getAccount();
        this.callingState = CallStateEnum.INCOMING_AUDIO_CALLING;

        findViews();

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_audio_call_request);
        setMuteSpeakerHangupControl(false);
        setRefuseReceive(true);
        receiveTV.setText(R.string.avchat_pickup);
    }

    public void showAudioInitLayout() {
        findViews();

        isInSwitch = false;
        showProfile();
        setSwitchVideo(true);
        setTime(true);
        hideNotify();
        setMuteSpeakerHangupControl(true);
        setRefuseReceive(false);
        enableToggle();
    }

    public void doOutGoingCall(String account) {
        // 拨打方的数据是account
        this.account = account;
        findViews();

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_wait_recieve);
        setMuteSpeakerHangupControl(true);
        setRefuseReceive(false);

        // 拨打音视频接口调用
        avChatController.doCalling(account, AVChatType.AUDIO, new AVChatControllerCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                avChatController.setAvChatData(avChatData);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                closeSession();
            }
        });
    }

    /**
     * ********************* 音视频切换 **********************
     */

    // 视频切换为音频时，禁音与扬声器按钮状态
    public void onVideoToAudio(boolean muteOn, boolean speakerOn) {
        showAudioInitLayout();

        muteToggle.toggle(muteOn ? ToggleState.ON : ToggleState.OFF);
        speakerToggle.toggle(speakerOn ? ToggleState.ON : ToggleState.OFF);
        recordToggle.setSelected(avChatController.isRecording());

        showRecordView(avChatController.isRecording(), isRecordWarning);
    }

    public void showIncomingAudioToVideo() {
        callingState = CallStateEnum.INCOMING_AUDIO_TO_VIDEO;

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_audio_call_request);
        setMuteSpeakerHangupControl(false);
        setRefuseReceive(true);
        receiveTV.setText(R.string.avchat_pickup);
    }


    /**
     * *************** 界面变化 *****************
     */

    // 显示或隐藏音视频切换
    private void setSwitchVideo(boolean visible) {
        switchVideo.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // 界面状态文案设置
    private void showNotify(int resId) {
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    //
    private void showProfile() {
        // TODO: 2018/8/3 0003 个人信息
//        headImg.loadBuddyAvatar(avChatController.getAvChatData() == null ? account
//                : avChatController.getAvChatData().getAccount());
        nickNameTV.setText(displayName);
    }

    // 显示或隐藏禁音，结束通话布局
    private void setMuteSpeakerHangupControl(boolean visible) {
        mute_speaker_hangup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // 显示或隐藏拒绝，开启布局
    private void setRefuseReceive(boolean visible) {
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // 设置通话时间显示
    private void setTime(boolean visible) {
        time.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            time.setBase(avChatController.getTimeBase());
            time.start();
        }
    }

    // 隐藏界面文案
    private void hideNotify() {
        notifyTV.setVisibility(View.GONE);
    }

    private void enableToggle() {
        if (!isEnabled) {
            recordToggle.setEnabled(true);
        }
        isEnabled = true;
    }

    /**
     * ************************ 点击事件 **************************
     */

    @Override
    public void toggleOn(View v) {
        onClick(v);
    }

    @Override
    public void toggleOff(View v) {
        onClick(v);
    }

    @Override
    public void toggleDisable(View v) {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.refuse) {
            doRefuseCall();
        } else if (i == R.id.receive) {
            doReceiveCall();
        } else if (i == R.id.avchat_audio_hangup) {
            doHangUp();
        } else if (i == R.id.avchat_audio_mute) {
            avChatController.toggleMute();
        } else if (i == R.id.avchat_audio_speaker) {
            avChatController.toggleSpeaker();
        } else if (i == R.id.avchat_audio_record) {
            doRecording();
        } else if (i == R.id.avchat_audio_switch_video) {
            if (isInSwitch) {
                Toast.makeText(context, R.string.avchat_in_switch, Toast.LENGTH_SHORT).show();
            } else {
                avChatController.switchAudioToVideo(avSwitchListener);
            }
        }
    }

    // 拒绝来电
    private void doRefuseCall() {
        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {
            avChatController.hangUp(AVChatExitCode.HANGUP);
            closeSession();
        } else if (callingState == CallStateEnum.INCOMING_AUDIO_TO_VIDEO) {
            rejectAudioToVideo();
        }
    }

    // 拒绝音视频切换
    private void rejectAudioToVideo() {
        AVChatManager.getInstance().sendControlCommand(avChatController.getAvChatData().getChatId(), AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_REJECT, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "rejectAudioToVideo success");
                showAudioInitLayout();
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "rejectAudioToVideo onFailed");

            }

            @Override
            public void onException(Throwable exception) {
                Log.i(TAG, "rejectAudioToVideo onException");
            }
        });
    }

    // 接听来电
    private void doReceiveCall() {
        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {
            showNotify(R.string.avchat_connecting);

            avChatController.receive(AVChatType.AUDIO, new AVChatControllerCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    closeSession();
                }
            });
        } else if (callingState == CallStateEnum.INCOMING_AUDIO_TO_VIDEO) {
            avChatController.receiveAudioToVideo(avSwitchListener);
        }

    }

    // 通话过程中，挂断电话
    private void doHangUp() {
        avChatController.hangUp(AVChatExitCode.HANGUP);
        closeSession();
    }

    private void doRecording() {
        avChatController.toggleRecord(AVChatType.AUDIO.getValue(), account, new AVChatController.RecordCallback() {
            @Override
            public void onRecordUpdate(boolean isRecording) {
                showRecordView(isRecording, isRecordWarning);
            }
        });
    }

    public void showRecordView(boolean show, boolean warning) {
        if (show) {
            recordToggle.setSelected(true);
            recordToggleButton.setText("结束");
            recordView.setVisibility(View.VISIBLE);
            recordTip.setVisibility(View.VISIBLE);
            if (warning) {
                recordWarning.setVisibility(View.VISIBLE);
            } else {
                recordWarning.setVisibility(View.GONE);
            }
        } else {
            recordToggle.setSelected(false);
            recordToggleButton.setText("录制");
            recordView.setVisibility(View.INVISIBLE);
            recordTip.setVisibility(View.INVISIBLE);
            recordWarning.setVisibility(View.GONE);
        }
    }

    public void showRecordWarning() {
        isRecordWarning = true;
        showRecordView(avChatController.isRecording(), isRecordWarning);
    }

    public void resetRecordTip() {
        isRecordWarning = false;
        avChatController.setRecording(false);
        showRecordView(false, isRecordWarning);
    }


    private void closeSession() {
        ((AVChatActivity) context).finish();
    }
}
