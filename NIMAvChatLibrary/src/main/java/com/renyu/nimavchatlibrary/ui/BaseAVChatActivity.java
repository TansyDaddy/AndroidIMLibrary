package com.renyu.nimavchatlibrary.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.renyu.nimavchatlibrary.impl.WebAppImpl;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    WebView web_webview;

    private String url;

    WebAppImpl impl;

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

        String extendMessage = getIntent().getStringExtra(KEY_EXTEND_MESSAGE);
        try {
            JSONObject jsonObject = new JSONObject(extendMessage);
            url = jsonObject.getString("vrurl");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        web_webview = findViewById(R.id.web_webview);
        web_webview.setSaveEnabled(true);
        web_webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        web_webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
        WebSettings settings=web_webview.getSettings();
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(false);
        impl=getIntent().getParcelableExtra("WebAppImpl");
        if (impl!=null) {
            impl.setContext(this);
            impl.setWebView(web_webview);
            web_webview.addJavascriptInterface(impl, getIntent().getStringExtra("WebAppImplName"));
        }
        web_webview.removeJavascriptInterface("searchBoxJavaBridge_");
        web_webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 接受所有网站的证书
                handler.proceed();
                super.onReceivedSslError(view, handler, error);
            }
        });
        // 设置cookies
        HashMap<String, String> cookies = new HashMap<>();
        if (getIntent().getStringExtra("cookieUrl") != null) {
            ArrayList<String> cookieValues = getIntent().getStringArrayListExtra("cookieValues");
            for (int i = 0; i < cookieValues.size()/2; i++) {
                cookies.put(cookieValues.get(i*2), cookieValues.get(i*2+1));
            }
            // cookies同步方法要在WebView的setting设置完之后调用，否则无效。
            syncCookie(this, getIntent().getStringExtra("cookieUrl"), cookies);
        }
        web_webview.loadUrl(url);

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
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

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

        if (web_webview!=null) {
            ViewParent parent = web_webview.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(web_webview);
            }
            web_webview.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            web_webview.getSettings().setJavaScriptEnabled(false);
            web_webview.clearHistory();
            web_webview.clearView();
            web_webview.removeAllViews();
            try {
                web_webview.destroy();
            } catch (Throwable ex) {

            }
        }

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

    /**
     * 添加Cookie
     * @param context
     * @param url
     * @param cookies
     */
    private void syncCookie(Context context, String url, HashMap<String, String> cookies) {
        // 如果API是21以下的话，需要在CookieManager前加
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        Iterator it = cookies.entrySet().iterator();
        // 注意使用for循环进行setCookie(String url, String value)调用。网上有博客表示使用分号手动拼接的value值会导致cookie不能完整设置或者无效
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String value = entry.getKey() + "=" + entry.getValue();
            cookieManager.setCookie(url, value);
        }
        // 如果API是21以下的话,在for循环结束后加
        CookieSyncManager.getInstance().sync();
    }
}
