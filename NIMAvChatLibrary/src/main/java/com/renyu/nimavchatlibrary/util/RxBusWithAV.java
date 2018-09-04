package com.renyu.nimavchatlibrary.util;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBusWithAV {
    private final Subject<Object> mBus;

    private RxBusWithAV() {
        mBus = PublishSubject.create();
    }

    public static RxBusWithAV getDefault() {
        return RxBusHolder.sInstance;
    }

    private static class RxBusHolder {
        private static final RxBusWithAV sInstance = new RxBusWithAV();
    }

    public void post(Object o) {
        mBus.onNext(o);
    }

    public <T> Observable<T> toObservable(Class<T> eventType) {
        return mBus.ofType(eventType);
    }
}
