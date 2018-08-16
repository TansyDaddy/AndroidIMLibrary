package com.renyu.nimavchatlibrary.ui;

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
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.constant.AVChatExitCode;
import com.renyu.nimavchatlibrary.constant.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public abstract class BaseAVChatActivity extends AppCompatActivity implements BaseAVManager.AVChatTypeListener {

    public abstract BaseAVManager initBaseAVManager();
    public abstract void registerObserver();
    public abstract void unregisterObserver();

    BaseAVManager manager;

    static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    static final String KEY_NEEDCALL = "KEY_NEEDCALL";
    static final String KEY_EXTEND_MESSAGE = "extendMessage";

    static boolean needFinish = true;
    // 是否暂停音视频
    boolean hasOnPause = false;

    Button btn_avchat;
    Button btn_avchat_send;
    TextView text_avchat_receive;

    // VR带看中的自定义消息
    Observer<CustomNotification> observer = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            try {
                JSONObject contentJson = new JSONObject(customNotification.getContent());
                if (contentJson.getString("type").equals("VR")) {
                    text_avchat_receive.setText(contentJson.getString("content"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 来电广播
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                Log.d("NIM_AV_APP", "收到来电");
                manager.hangUp(AVChatExitCode.HANGUP);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avchat);

        text_avchat_receive = findViewById(R.id.text_avchat_receive);
        btn_avchat = findViewById(R.id.btn_avchat);
        btn_avchat_send = findViewById(R.id.btn_avchat_send);
        btn_avchat_send.setOnClickListener(v -> {
            // 如果正在聊天，则可以发送自定义信息
            if (BaseAVManager.avChatData != null && BaseAVManager.isCallEstablish.get()) {
                CustomNotification command = new CustomNotification();
                command.setSessionId(BaseAVManager.avChatData.getAccount());
                command.setSessionType(SessionTypeEnum.P2P);
                CustomNotificationConfig config = new CustomNotificationConfig();
                config.enablePush = false;
                config.enableUnreadCount = false;
                command.setConfig(config);
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "VR");
                    json.put("content", "VR自定义消息："+new Date().toString());
                    command.setContent(json.toString());
                    NIMClient.getService(MsgService.class).sendCustomNotification(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
        if (needFinish) {
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // 开启自定义消息通道
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observer, true);

        manager = initBaseAVManager();
        manager.setAvChatTypeListener(this);
        // 注册监听
        registerObserver();
        manager.registerCommonObserver(true);

        // 通话状态广播
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
            unregisterObserver();
            manager.registerCommonObserver(false);
            // 重置参数
            manager.reSetParams();
        }
        needFinish = true;

        unregisterReceiver(receiver);

        // 关闭自定义消息通道
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observer, false);
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
    }

    @Override
    public void chatTypeChange(AVChatTypeEnum avChatTypeEnum) {
        switch (avChatTypeEnum) {
            case CONN:
                btn_avchat.setText("正在呼叫，点击关闭");
                btn_avchat.setOnClickListener(v -> {
                    manager.hangUp(AVChatExitCode.CANCEL);
                    finish();
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
