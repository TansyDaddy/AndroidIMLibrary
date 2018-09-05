package com.renyu.nimapp.ui.app

import android.support.multidex.MultiDexApplication
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco
import com.netease.nimlib.sdk.util.NIMUtil
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimavchatlibrary.manager.InComingAVManager
import com.renyu.nimlibrary.manager.AuthManager

class ExampleApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // 初始化工具库
        Utils.init(this)

        // 初始化Fresco
        Fresco.initialize(this)

        // 初始化百度地图
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)

        // 初始化云信
        AuthManager.init(NimInitParams.SDKROOT, NimInitParams.databaseEncryptKey)

        if (NIMUtil.isMainProcess(this)) {
            // 配置基础监听
            AuthManager.initObserve()
            // 音频通话接听配置
            if (NimInitParams.isAgent) {
                InComingAVManager.inComingAVManager = InComingAVManager()
                InComingAVManager.inComingAVManager.registerInComingObserver()
            }
        }
    }
}