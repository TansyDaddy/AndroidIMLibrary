package com.renyu.nimavchatlibrary.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.renyu.nimavchatlibrary.impl.WebAppInterface;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.manager.InComingAVManager;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;

public class InComingAVChatActivity extends BaseAVChatActivity {

    @Override
    public BaseAVManager initBaseAVManager() {
        return InComingAVManager.inComingAVManager;
    }

    @Override
    public void registerObserver() {

    }

    @Override
    public void unregisterObserver() {

    }

    /**
     * 被叫
     * @param context
     * @param account
     * @param extendMessage
     * @param receive
     */
    public static void incomingCall(Context context, String account, String extendMessage, boolean receive) {
        needFinish = false;
        Intent intent = new Intent(context, InComingAVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_EXTEND_MESSAGE, extendMessage);
        intent.putExtra(KEY_RECEIVE, receive);
        intent.putExtra("WebAppImplName", "android");
        intent.putExtra("WebAppImpl", new WebAppInterface());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 来电的时候要判断是不是真的是当前会话人在呼叫
        if (BaseAVManager.avChatData != null && BaseAVManager.avChatData.getAccount().equals(getIntent().getStringExtra(KEY_ACCOUNT))) {
            if (getIntent().getBooleanExtra("KEY_RECEIVE", false)) {
                // 接听电话
                manager.receive();
            }
            else {
                // 挂断电话
                manager.hangUp(AVChatExitCode.REJECT);
                finish();
            }
        }
    }
}
