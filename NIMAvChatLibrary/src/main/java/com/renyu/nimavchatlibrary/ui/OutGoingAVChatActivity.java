package com.renyu.nimavchatlibrary.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.renyu.nimavchatlibrary.impl.WebAppInterface;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.manager.OutGoingAVManager;

public class OutGoingAVChatActivity extends BaseAVChatActivity {

    @Override
    public BaseAVManager initBaseAVManager() {
        return new OutGoingAVManager();
    }

    @Override
    public void registerObserver() {
        ((OutGoingAVManager) manager).registerOutgoingObserver(true);
    }

    @Override
    public void unregisterObserver() {
        ((OutGoingAVManager) manager).registerOutgoingObserver(false);
    }

    /**
     * 主叫
     * @param context
     * @param account
     * @param extendMessage
     * @param needCall
     */
    public static void outgoingCall(Context context, String account, String extendMessage, boolean needCall) {
        Intent intent = new Intent(context, OutGoingAVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_EXTEND_MESSAGE, extendMessage);
        intent.putExtra(KEY_NEEDCALL, needCall);
        intent.putExtra("WebAppImplName", "android");
        intent.putExtra("WebAppImpl", new WebAppInterface());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 去电的时候需要判断是不是需要发生去电行为
        if (getIntent().getBooleanExtra("KEY_NEEDCALL", false)) {
            manager.call(getIntent().getStringExtra(KEY_ACCOUNT), getIntent().getStringExtra(KEY_EXTEND_MESSAGE));
        }
    }
}
