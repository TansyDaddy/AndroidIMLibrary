package com.renyu.nimavchatlibrary.impl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.renyu.nimavchatlibrary.params.AVChatExitCode;
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum;
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
     * 音频功能点击
     */
    @JavascriptInterface
    public void avChatClick() {
        ((BaseAVChatActivity) context).chatTypeChangeClick();
    }

    /**
     * 切换静音状态
     */
    @JavascriptInterface
    public void avChatMuteClick() {
        ((BaseAVChatActivity) context).toggleMute();
    }

    /**
     * 接收自定义消息
     * @param string
     */
    public void receiverMessage(String string) {
        webView.post(() -> webView.loadUrl("javascript:changeConOne('"+string+"')"));
    }

    /**
     * 更新音频功能状态显示
     * @param string
     */
    public void updateVRStatus(String string) {
        webView.post(() -> webView.loadUrl("javascript:changeConTwo('"+string+"')"));
    }

    /**
     * 更新静音状态
     * @param string
     */
    public void updateMuteStatues(String string) {
        webView.post(() -> webView.loadUrl("javascript:changeConThree('"+string+"')"));
    }







    /**
     * 静音
     */
    @JavascriptInterface
    public void mute() {
        ((BaseAVChatActivity) context).toggleMute();
    }

    /**
     * 非静音
     */
    @JavascriptInterface
    public void unmute() {
        ((BaseAVChatActivity) context).toggleMute();
    }

    /**
     * 发送自定义消息
     * @param string
     */
    @JavascriptInterface
    public void sendData(String string) {
        ((BaseAVChatActivity) context).sendCustomNotification(string);
    }

    /**
     * 接收自定义消息
     * @param string
     */
    public void onData(String string) {
        webView.loadUrl("javascript:onData('"+string+"')");
    }

    /**
     * 呼叫
     */
    @JavascriptInterface
    public void call(String account, String extendMessage) {
        ((BaseAVChatActivity) context).manager.call(account, extendMessage);
    }

    /**
     * 挂断电话
     */
    @JavascriptInterface
    public void hangup() {
        ((BaseAVChatActivity) context).manager.hangUp(AVChatExitCode.HANGUP);
        ((BaseAVChatActivity) context).finish();
    }

    /**
     * 更新众趣页面状态
     * @param avChatTypeEnum
     */
    public void showAcceptOrRefuse(AVChatTypeEnum avChatTypeEnum) {
        //  C端正在呼叫       outGoing
        //  C端主叫连接成功   outGoingSucess
        //  B端被叫连接成功   inComingSucess
        //  C端占线           outGoingBusy
        //  C端被拒接         outGoingReject
        //  C端60s超时        outGoingTimeout
        //  C端被挂断         outGoingFinish
        //  B端被挂断         inComingFinish
        //  音频功能出错      error
        webView.loadUrl("javascript:showAcceptOrRefuse ('"+""+"')");
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
