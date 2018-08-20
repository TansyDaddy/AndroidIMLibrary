package com.renyu.nimavchatlibrary.module;


import android.os.Handler;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 音视频通话时超时监听处理
 * Created by weilv on 17/9/11.
 */

public class AVChatTimeoutObserver {

    private List<TimeoutObserver> timeoutObservers = new ArrayList<>();
    private List<Observer<Integer>> timeoutObserverLocal = new ArrayList<>(1); // 来电or呼出超时监听
    private Handler uiHandler;

    private static class InstanceHolder {
        final static AVChatTimeoutObserver instance = new AVChatTimeoutObserver();
    }

    public static AVChatTimeoutObserver getInstance() {
        return InstanceHolder.instance;
    }

    private AVChatTimeoutObserver() {
        uiHandler = new Handler(Utils.getApp().getMainLooper());
    }

    // 通知APP观察者
    private <T> void notifyObservers(List<Observer<T>> observers, T result) {
        if (observers == null || observers.isEmpty()) {
            return;
        }

        // 创建副本，为了使得回调到app后，app如果立即注销观察者，会造成List异常。
        List<Observer<T>> copy = new ArrayList<>(observers.size());
        copy.addAll(observers);

        for (Observer<T> o : copy) {
            o.onEvent(result);
        }
    }

    // 注册注销APP观察者
    private <T> void registerObservers(List<Observer<T>> observers, final Observer<T> observer, boolean register) {
        if (observers == null || observer == null) {
            return;
        }

        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public void observeTimeoutNotification(Observer<Integer> observer, boolean register) {
        registerObservers(timeoutObserverLocal, observer, register);
        if (register) {
            addTimeout();
        } else {
            removeAllTimeout();
        }
    }

    private class TimeoutObserver implements Runnable {
        @Override
        public void run() {
            notifyObservers(timeoutObserverLocal, 0);
        }
    }

    private void addTimeout() {
        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);
        int OUTGOING_TIME_OUT = 60 * 1000;
        uiHandler.postDelayed(timeoutObserver, OUTGOING_TIME_OUT);
    }

    private void removeAllTimeout() {
        for (TimeoutObserver observer : timeoutObservers) {
            uiHandler.removeCallbacks(observer);
        }
        timeoutObservers.clear();
    }
}
