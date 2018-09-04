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
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest;
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.renyu.nimavchatlibrary.R;
import com.renyu.nimavchatlibrary.impl.WebAppImpl;
import com.renyu.nimavchatlibrary.impl.WebAppInterface;
import com.renyu.nimavchatlibrary.manager.BaseAVManager;
import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kotlin.Triple;

public abstract class BaseAVChatActivity extends AppCompatActivity implements BaseAVManager.AVChatTypeListener, BaseAVManager.AVChatMuteListener {

    public abstract BaseAVManager initBaseAVManager();
    public abstract void registerObserver();
    public abstract void unregisterObserver();

    static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    static final String KEY_NEEDCALL = "KEY_NEEDCALL";
    static final String KEY_EXTEND_MESSAGE = "extendMessage";

    static boolean needFinish = true;
    // 是否暂停音频
    boolean hasOnPause = false;
    BaseAVManager manager;
    // 监听事件
    EventSubscribeRequest eventSubscribeRequest;
    Observer<List<Event>> observeEventChanged;
    // 当前通话状态
    AVChatTypeEnum avChatType = AVChatTypeEnum.VALID;

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
                    if (impl != null) {
                        ((WebAppInterface) impl).receiverMessage(contentJson.getString("content"));
                    }
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
            url = jsonObject.getString("vrUrl");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
        if (needFinish) {
            finish();
            return;
        }

        // 加载webview相关
        initWebView();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        manager = initBaseAVManager();

        initAVChatConfig();
    }

    /**
     * 初始化音频消息配置
     */
    private void initAVChatConfig() {
        // 注册通话状态广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiver, filter);
        // 注册监听
        manager.setAvChatTypeListener(this);
        startAVChatConfig();
    }

    /**
     * 在当前页面开启音频消息配置
     */
    private void startAVChatConfig() {
        // 注销音频主叫、被叫公共观察者
        manager.registerCommonObserver(true);
        // 注销音频主叫、被叫特定观察者
        registerObserver();
        // 监听音频聊天对方在线情况与自定义消息通道
        ArrayList<String> accounts = new ArrayList<>();
        accounts.add(getIntent().getStringExtra(KEY_ACCOUNT));
        subscribeEvent(accounts);
        // 注册监听
        manager.setAVChatMuteListener(this);
    }

    /**
     * 终止音频消息配置
     */
    private void terminatedAVChatConfig() {
        // 注销通话状态广播
        unregisterReceiver(receiver);
        endAVChatConfig();
        // 注销监听
        manager.setAvChatTypeListener(null);
    }

    /**
     * 在当前页面结束音频消息配置
     */
    private void endAVChatConfig() {
        // 重置参数
        manager.reSetParams();
        // 注销音频主叫、被叫公共观察者
        manager.registerCommonObserver(false);
        // 注销音频主叫、被叫特定观察者
        unregisterObserver();
        // 注销音频聊天对方在线情况与自定义消息通道
        unsubscribeEvent();
        // 注销监听
        manager.setAVChatMuteListener(null);
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
            terminatedAVChatConfig();
        }

        destoryWebView();

        needFinish = true;
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
    }

    // 订阅有效期 1天，单位秒
    private static final long SUBSCRIBE_EXPIRY = 60 * 60 * 24;

    /**
     * 订阅指定账号的在线状态事件
     */
    private void subscribeEvent(List<String> accounts) {
        eventSubscribeRequest = new EventSubscribeRequest();
        eventSubscribeRequest.setEventType(NimOnlineStateEvent.EVENT_TYPE);
        eventSubscribeRequest.setPublishers(accounts);
        eventSubscribeRequest.setExpiry(SUBSCRIBE_EXPIRY);
        eventSubscribeRequest.setSyncCurrentValue(true);
        NIMClient.getService(EventSubscribeService.class).subscribeEvent(eventSubscribeRequest)
                .setCallback(new RequestCallbackWrapper<List<String>> () {
                    @Override
                    public void onResult(int code, List<String> result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            if (result != null) {

                            }
                        }
                    }
                });
        observeEventChanged = (Observer<List<Event>>) events -> {
            for (Event event : events) {
                if (NimOnlineStateEvent.isOnlineStateEvent(event)) {
                    try {
                        if (new JSONObject(event.getNimConfig()).getJSONArray("online").length() == 0) {
                            Log.d("NIM_APP", event.getPublisherAccount()+"下线");
                            // 如果正在通话中，就挂断电话
                            if (BaseAVManager.isCallEstablish.get()) {
                                manager.hangUp(AVChatExitCode.HANGUP);
                            }
                            finish();
                        } else {
                            Log.d("NIM_APP", event.getPublisherAccount()+"上线："+new JSONObject(event.getNimConfig()).getJSONArray("online").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        NIMClient.getService(EventSubscribeServiceObserver.class).observeEventChanged(observeEventChanged, true);
        // 开启自定义消息通道
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observer, true);
    }

    private void unsubscribeEvent() {
        NIMClient.getService(EventSubscribeService.class).unSubscribeEvent(eventSubscribeRequest);
        NIMClient.getService(EventSubscribeServiceObserver.class).observeEventChanged(observeEventChanged, false);
        // 关闭自定义消息通道
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(observer, false);
    }

    public void sendCustomNotification(String string) {
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
                json.put("content", "VR自定义消息："+string);
                command.setContent(json.toString());
                NIMClient.getService(MsgService.class).sendCustomNotification(command);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 切换静音状态
     */
    public void toggleMute() {
        manager.toggleMute();
    }

    @Override
    public void chatTypeChange(AVChatTypeEnum avChatTypeEnum) {
        if (avChatTypeEnum == AVChatTypeEnum.CALLEE_ACK_REQUEST) {
            // 该页面来电的时候app不在前台就关闭
            if (!AppUtils.isAppForeground()) {
                finish();
            }
            else {
                // 该页面来电的时候要判断是不是之前的C端用户所呼叫的房源
                if (BaseAVManager.avChatData != null
                        && BaseAVManager.avChatData.getExtra().equals(getIntent().getStringExtra(KEY_EXTEND_MESSAGE))
                        && BaseAVManager.avChatData.getAccount().equals(getIntent().getStringExtra(KEY_ACCOUNT))) {
                    this.avChatType = avChatTypeEnum;

                    // 当前页面收到呼叫，重置各种监听事件
                    startAVChatConfig();
                    // 直接接听电话
                    manager.receive();
                }
                // 若不是，则关闭当前页面
                else {
                    finish();
                }
            }
        }
        else {
            this.avChatType = avChatTypeEnum;
            chatTypeChangeUI(avChatTypeEnum);
        }
    }

    /**
     * 根据状态刷新UI
     * @param avChatTypeEnum
     */
    public void chatTypeChangeUI(AVChatTypeEnum avChatTypeEnum) {
        boolean isAgent = getUserRole() == 1;
        switch (avChatTypeEnum) {
            case CONN:
                // 根据主叫或者被叫区分默认点击功能
                if (!isAgent) {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("正在呼叫，点击挂断");
                    }
                } else {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("正在被叫，点击挂断");
                    }
                }
                break;
            case CONFIG_ERROR:
                if (impl != null) {
                    ((WebAppInterface) impl).updateVRStatus("出错，点击关闭");
                }
                break;
            case PEER_HANG_UP:
                // 根据主叫或者被叫区分默认点击功能
                if (!isAgent) {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("对方已挂断，点击呼叫");
                    }
                } else {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("对方已挂断，点击关闭");
                    }
                }
                endAVChatConfig();
                break;
            case PEER_NO_RESPONSE:
                if (!isAgent) {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("已超时，点击呼叫");
                    }
                    endAVChatConfig();
                }
                break;
            case INVALIDE_CHANNELID:
                if (impl != null) {
                    ((WebAppInterface) impl).updateVRStatus("聊天ID错误");
                }
                break;
            case CALLEE_ACK_AGREE:
                if (impl != null) {
                    ((WebAppInterface) impl).updateVRStatus("正在通话，点击挂断");
                }
                break;
            case CALLEE_ACK_BUSY:
                if (impl != null) {
                    ((WebAppInterface) impl).updateVRStatus("正在呼叫，点击挂断");
                    Toast.makeText(this, "安家顾问（经纪人）暂时忙，请稍后呼叫", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                // 根据主叫或者被叫区分默认点击功能
                if (!isAgent) {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("点击呼叫");
                    }
                } else {
                    if (impl != null) {
                        ((WebAppInterface) impl).updateVRStatus("暂无连接，点击关闭");
                    }
                }
        }
    }

    /**
     * 根据状态刷新点击事件
     */
    public void chatTypeChangeClick() {
        boolean isAgent = getUserRole() == 1;
        switch (avChatType) {
            case CONN:
                manager.hangUp(AVChatExitCode.CANCEL);
                finish();
                break;
            case CONFIG_ERROR:
                finish();
                break;
            case PEER_HANG_UP:
                if (!isAgent) {
                    startAVChatConfig();
                    manager.call(getIntent().getStringExtra(KEY_ACCOUNT), getIntent().getStringExtra(KEY_EXTEND_MESSAGE));
                } else {
                    finish();
                }
                break;
            case PEER_NO_RESPONSE:
                if (!isAgent) {
                    startAVChatConfig();
                    manager.call(getIntent().getStringExtra(KEY_ACCOUNT), getIntent().getStringExtra(KEY_EXTEND_MESSAGE));
                }
                break;
            case INVALIDE_CHANNELID:
                finish();
                break;
            case CALLEE_ACK_AGREE:
                manager.hangUp(AVChatExitCode.HANGUP);
                finish();
                break;
            case CALLEE_ACK_BUSY:
                manager.hangUp(AVChatExitCode.CANCEL);
                finish();
                break;
            default:
                // 根据主叫或者被叫区分默认点击功能
                if (!isAgent) {
                    manager.call(getIntent().getStringExtra(KEY_ACCOUNT), getIntent().getStringExtra(KEY_EXTEND_MESSAGE));
                } else {
                    finish();
                }
        }
    }

    @Override
    public void chatMuteChange(boolean mute) {
        if (mute) {
            ((WebAppInterface) impl).updateMuteStatues("已静音");
        }
        else {
            ((WebAppInterface) impl).updateMuteStatues("非静音");
        }
    }

    private void initWebView() {
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
                // 如果页面在加载完毕之前已经发生状态变化，则忽略
                if (avChatType != AVChatTypeEnum.VALID) {

                }
                else {
                    // 页面加载完毕之后根据主叫或者被叫区分状态内容
                    chatTypeChange(AVChatTypeEnum.VALID);
                }
                ((WebAppInterface) impl).updateMuteStatues("非静音");
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
        web_webview.loadUrl("file:///android_asset/index.html");
    }

    private void destoryWebView() {
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

    /**
     * 获取当前登录用户角色
     * @return
     */
    public Integer getUserRole() {
        try {
            Class userManager = Class.forName("com.renyu.nimlibrary.manager.UserManager");
            Method getUserAccount = userManager.getDeclaredMethod("getUserAccount");
            getUserAccount.setAccessible(true);
            Triple triple = (Triple) getUserAccount.invoke(userManager);
            triple.getThird();

            Class userRole = Class.forName("com.renyu.nimlibrary.manager.UserManager$UserRole");
            Method getRole = userRole.getDeclaredMethod("getRole");
            getRole.setAccessible(true);
            Integer integer = (Integer) getRole.invoke(triple.getThird());
//            if (integer == 0) {
//                Log.d("NIM_AV_APP", "未知");
//            }
//            if (integer == 1) {
//                Log.d("NIM_AV_APP", "经纪人");
//            }
//            if (integer == 2) {
//                Log.d("NIM_AV_APP", "客户");
//            }
            return integer;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }
}