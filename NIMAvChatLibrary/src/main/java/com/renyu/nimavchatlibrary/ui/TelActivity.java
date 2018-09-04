package com.renyu.nimavchatlibrary.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.Utils;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.manager.InComingAVManager;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;
import com.renyu.nimavchatlibrary.util.RxBusWithAV;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class TelActivity extends Activity {

    Disposable disposable;

    public static void gotoTelActivity() {
        // 解决后台启动activity缓慢的问题
        Intent intent = new Intent(Utils.getApp(), TelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(Utils.getApp(), 0, intent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tel);

        disposable = RxBusWithAV.getDefault()
                .toObservable(AVChatTypeEnum.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(avChatTypeEnum -> {
                    // 超时，无人接听
                    // 对方挂断电话
                    // 验证失败
                    // 无效的聊天ID
                    if (avChatTypeEnum == AVChatTypeEnum.PEER_NO_RESPONSE ||
                            avChatTypeEnum == AVChatTypeEnum.PEER_HANG_UP ||
                            avChatTypeEnum == AVChatTypeEnum.CONFIG_ERROR ||
                            avChatTypeEnum == AVChatTypeEnum.INVALIDE_CHANNELID) {
                        finish();
                    }
                }).subscribe();

        // 接听
        findViewById(R.id.btn_receive).setOnClickListener(v -> {
            if (BaseAVManager.avChatData == null) {
                finish();
                return;
            }
            InComingAVChatActivity.incomingCall(TelActivity.this,
                    BaseAVManager.avChatData.getAccount(),
                    BaseAVManager.avChatData.getExtra());
        });

        // 拒绝
        findViewById(R.id.btn_reject).setOnClickListener(v -> {
            if (BaseAVManager.avChatData != null) {
                InComingAVManager.inComingAVManager.hangUp(AVChatExitCode.HANGUP);
            }
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
    }
}
