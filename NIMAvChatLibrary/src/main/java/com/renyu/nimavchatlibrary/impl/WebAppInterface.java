package com.renyu.nimavchatlibrary.impl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.renyu.nimavchatlibrary.ui.BaseAVChatActivity;

/**
 * Created by renyu on 2017/8/14.
 */

public class WebAppInterface implements Parcelable, WebAppImpl {

    private Context context;
    private WebView webView;

    public WebAppInterface() {
        super();
    }

    private WebAppInterface(Parcel in) {

    }

    public static final Creator<WebAppInterface> CREATOR = new Creator<WebAppInterface>() {
        @Override
        public WebAppInterface createFromParcel(Parcel in) {
            return new WebAppInterface(in);
        }

        @Override
        public WebAppInterface[] newArray(int size) {
            return new WebAppInterface[size];
        }
    };

    /**
     * 发送自定义消息
     * @param string
     */
    @JavascriptInterface
    public void sendCustomNotification(String string) {
        ((BaseAVChatActivity) context).sendCustomNotification(string);
    }

    /**
     * 音视频功能点击
     */
    @JavascriptInterface
    public void avChatClick() {
        ((BaseAVChatActivity) context).chatTypeChangeClick();
    }

    /**
     * 接收自定义消息
     * @param string
     */
    public void receiverMessage(String string) {
        webView.loadUrl("javascript:changeConOne('"+string+"')");
    }

    /**
     * 更新音视频功能状态显示
     * @param string
     */
    public void updateVRStatus(String string) {
        webView.loadUrl("javascript:changeConTwo('"+string+"')");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @Override
    public void setContext(Context context) {
        this.context=context;
    }

    @Override
    public void setWebView(WebView webView) {
        this.webView=webView;
    }
}
