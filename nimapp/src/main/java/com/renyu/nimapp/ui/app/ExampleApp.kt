package com.renyu.nimapp.ui.app

import android.support.multidex.MultiDexApplication
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco
import com.netease.nimlib.sdk.util.NIMUtil
import com.renyu.nimapp.params.InitParams
import com.renyu.nimavchatlibrary.manager.InComingAVManager
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.params.CommonParams

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
        AuthManager.init(CommonParams.SDKROOT, "house365")

        if (NIMUtil.isMainProcess(this)) {
            // 配置基础监听
            AuthManager.initObserve()
            // 音频通话接听配置
            if (InitParams.isAgent) {
                InComingAVManager.inComingAVManager = InComingAVManager()
                InComingAVManager.inComingAVManager.registerInComingObserver()
            }
            // 假登录获取本地数据
//            AuthManager.fakeLogin()
        }
    }
}