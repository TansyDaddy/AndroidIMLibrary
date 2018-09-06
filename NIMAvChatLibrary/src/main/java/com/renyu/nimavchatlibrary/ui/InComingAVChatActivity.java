package com.renyu.nimavchatlibrary.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.renyu.nimavchatlibrary.impl.WebAppInterface;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.manager.InComingAVManager;

public class InComingAVChatActivity extends BaseAVChatActivity {

    public static InComingAVChatActivity inComingAVChatActivity = null;

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
     * @param activity
     * @param account
     * @param extendMessage
     */
    public static void incomingCall(Activity activity, String account, String extendMessage) {
        Intent intent = new Intent(activity, InComingAVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_EXTEND_MESSAGE, extendMessage);
        intent.putExtra("WebAppImplName", "house365js");
        intent.putExtra("WebAppImpl", new WebAppInterface());
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inComingAVChatActivity = this;

        // 来电的时候要判断是不是真的是当前会话人在呼叫
        if (BaseAVManager.avChatData != null && BaseAVManager.avChatData.getAccount().equals(getIntent().getStringExtra(KEY_ACCOUNT))) {
            // 接听电话
            manager.receive();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        inComingAVChatActivity = null;
    }
}
