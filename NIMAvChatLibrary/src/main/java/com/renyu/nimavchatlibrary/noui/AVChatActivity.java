package com.renyu.nimavchatlibrary.noui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.constant.AVChatExitCode;
import com.renyu.nimavchatlibrary.constant.CallStateEnum;
import com.renyu.nimavchatlibrary.controll.AVChatSoundPlayer;
import com.renyu.nimavchatlibrary.noui.manager.AVManager;
import com.renyu.nimavchatlibrary.noui.params.AVChatTypeEnum;

public class AVChatActivity extends AppCompatActivity implements AVManager.AVChatTypeListener {

    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    private static final String KEY_EXTEND_MESSAGE = "extendMessage";

    // 来自广播
    public static final int FROM_BROADCASTRECEIVER = 0;
    // 来自发起方
    public static final int FROM_INTERNAL = 1;
    // 未知的入口
    public static final int FROM_UNKNOWN = -1;

    private static boolean needFinish = true;
    // 区分音频或者视频
    private int state;
    // 是否暂停音视频
    private boolean hasOnPause = false;

    AVManager manager = null;

    Button btn_avchat;

    // 来电广播
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                Log.d("NIM_AV_APP", "收到来电");
                manager.hangUp(AVChatExitCode.HANGUP);
                finish();
            }
        }
    };

    /**
     * 拨打电话
     * @param context
     * @param callType
     * @param source
     */
    public static void outgoingCall(Context context, String account, String extendMessage, int callType, int source) {
        needFinish = false;

        Intent intent = new Intent(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_EXTEND_MESSAGE, extendMessage);
        intent.putExtra(KEY_IN_CALLING, false);
        intent.putExtra(KEY_CALL_TYPE, callType);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    /**
     * 接听电话
     * @param context
     * @param config
     * @param source
     */
    public static void incomingCall(Context context, String account, String extendMessage, int callType, AVChatData config, int source) {
        needFinish = false;
        Intent intent = new Intent(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_EXTEND_MESSAGE, extendMessage);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_CALL_TYPE, callType);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avchat);

        btn_avchat = findViewById(R.id.btn_avchat);

        // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
        if (needFinish) {
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        boolean mIsInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER:
                // 来电
                AVChatData avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
                state = avChatData.getChatType().getValue();
                manager = new AVManager(avChatData, mIsInComingCall, this);
                break;
            case FROM_INTERNAL:
                // 去电
                state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
                manager = new AVManager(null, mIsInComingCall, this);
                break;
            default:
                break;
        }

        // 注册监听
        manager.registerObserves(true);

        if (state == CallStateEnum.AUDIO.getValue()) {
            if (mIsInComingCall) {
                // 来电
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
                // 接听电话
                manager.receive();
            }
            else {
                // 去电
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
                // 拨打电话
                manager.doCalling(getIntent().getStringExtra(KEY_ACCOUNT), getIntent().getStringExtra(KEY_EXTEND_MESSAGE));
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasOnPause) {
            manager.resumeVideo();
            hasOnPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.pauseVideo();
        hasOnPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(manager != null){
            //界面销毁时强制尝试挂断，防止出现红米Note 4X等手机在切后台点击杀死程序时，实际没有杀死的情况
            manager.hangUp(AVChatExitCode.HANGUP);
            // 关闭所有监听
            manager.registerObserves(false);
        }
        // 设置当前没有接听音视频消息
        AVManager.setAVChatting(false);
        needFinish = true;

        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
    }

    @Override
    public void chatTypeChange(AVChatTypeEnum avChatTypeEnum) {
        switch (avChatTypeEnum) {
            case CONN:
                btn_avchat.setText("正在呼叫");
                btn_avchat.setOnClickListener(v -> {

                });
                break;
            case CONFIG_ERROR:
                btn_avchat.setText("出错，点击关闭");
                btn_avchat.setOnClickListener(v -> finish());
                break;
            case PEER_HANG_UP:
                btn_avchat.setText("已挂断，点击关闭");
                btn_avchat.setOnClickListener(v -> finish());
                break;
            case PEER_NO_RESPONSE:
                btn_avchat.setText("已超时，点击关闭");
                btn_avchat.setOnClickListener(v -> finish());
                break;
            case INVALIDE_CHANNELID:
                btn_avchat.setText("聊天ID错误");
                btn_avchat.setOnClickListener(v -> finish());
                break;
            case CALLEE_ACK_AGREE:
                btn_avchat.setText("正在通话，点击挂断");
                btn_avchat.setOnClickListener(v -> {
                    manager.hangUp(AVChatExitCode.HANGUP);
                    finish();
                });
                break;
            case CALLEE_ACK_REJECT:
                btn_avchat.setText("已被拒绝，点击关闭");
                btn_avchat.setOnClickListener(v -> finish());
                break;
            case CALLEE_ACK_BUSY:
                btn_avchat.setText("对方繁忙，点击关闭");
                btn_avchat.setOnClickListener(v -> finish());
                break;
        }
    }
}
