package com.renyu.nimlibrary.manager

import android.app.Activity
import android.graphics.Color
import android.text.TextUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.netease.nimlib.sdk.*
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.mixpush.MixPushConfig
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.activity.NotificationActivity

object AuthManager {

    fun init(sdkStorageRootPath: String, databaseEncryptKey: String) {
        val options = SDKOptions()
        // 配置 APP 保存图片/语音/文件/log等数据的目录
        options.sdkStorageRootPath = sdkStorageRootPath
        // 配置数据库加密秘钥
        options.databaseEncryptKey = databaseEncryptKey
        // 配置是否需要SDK自动预加载多媒体消息的附件
        options.preloadAttach = true
        // 配置附件缩略图的尺寸大小
        options.thumbnailSize =  ScreenUtils.getScreenWidth() / 2
        // 在线多端同步未读数
        options.sessionReadAck = true
        // 动图的缩略图直接下载原图
        options.animatedImageThumbnailEnabled = true
        // 采用异步加载SDK
        options.asyncInitSDK = true
        // 是否是弱IM场景
        options.reducedIM = false
        // 是否提高SDK进程优先级（默认提高，可以降低SDK核心进程被系统回收的概率）
        options.improveSDKProcessPriority = true
        // 预加载服务，默认true，不建议设置为false，预加载连接可以优化登陆流程
        options.preLoadServers = true
        // 是否在 SDK 初始化时检查清单文件配置是否完全，默认为 false，建议开发者在调试阶段打开，上线时关掉
        options.checkManifestConfig = true
        // 配置通知栏
        options.statusBarNotificationConfig = loadStatusBarNotificationConfig()
        options.mixPushConfig = buildMixPushConfig()

        NIMClient.init(Utils.getApp(), null, options)

        // 初始化消息提醒
        NIMClient.toggleNotification(true)
    }

    /**
     * 自定义该应用初始的 StatusBarNotificationConfig
     */
    private fun loadStatusBarNotificationConfig(): StatusBarNotificationConfig {
        val config = StatusBarNotificationConfig()

        val clazz = Class.forName("com.renyu.nimapp.params.InitParams")
        val initActivityName = clazz.getField("InitActivityName").get(clazz).toString()
        val initClass = Class.forName(initActivityName)

        // 点击通知需要跳转到的界面
        config.notificationEntrance = if (initClass != null) (initClass as Class<out Activity>) else NotificationActivity::class.java
        config.notificationSmallIconId = Integer.parseInt(clazz.getField("notificationIcon").get(clazz).toString())
        config.notificationColor = Integer.parseInt(clazz.getField("notificationColor").get(clazz).toString())
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.renyu.nimapp/raw/msg"
        config.notificationFolded = true
        // 呼吸灯配置
        config.ledARGB = Color.GREEN
        config.ledOnMs = 1000
        config.ledOffMs = 1500
        // 是否APP ICON显示未读数红点(Android O有效)
        config.showBadge = true
        return config
    }

    /**
     * 第三方推送配置
     */
    private fun buildMixPushConfig(): MixPushConfig {
        // 通过反射获取
        val clazz = Class.forName("com.renyu.nimapp.params.InitParams")

        val config = MixPushConfig()

        // 小米推送
        config.xmAppId = clazz.getField("xmAppId").get(clazz).toString()
        config.xmAppKey = clazz.getField("xmAppKey").get(clazz).toString()
        config.xmCertificateName = clazz.getField("xmCertificateName").get(clazz).toString()

        // 华为推送
        config.hwCertificateName = "DEMO_HW_PUSH"

        // 魅族推送
        config.mzAppId = "111710"
        config.mzAppKey = "282bdd3a37ec4f898f47c5bbbf9d2369"
        config.mzCertificateName = "DEMO_MZ_PUSH"

        return config
    }

    /**
     * 假登录获取本地数据
     */
    fun fakeLogin() {
        val account = SPUtils.getInstance().getString(CommonParams.SP_UNAME)
        if (!TextUtils.isEmpty(account)) {
            NIMClient.getService(AuthService::class.java).openLocalCache(account)
        }
    }

    /**
     * 登录
     */
    fun login(account: String, token: String, requestCallback: RequestCallback<LoginInfo>): AbortableFuture<out Any> {
        val loginRequest = NIMClient.getService(AuthService::class.java)
                .login(LoginInfo(account, token))
        loginRequest.setCallback(requestCallback)
        return loginRequest
    }

    /**
     * 登录
     */
    fun login(account: String, token: String) {
        NIMClient.getService(AuthService::class.java).login(LoginInfo(account, token))
    }

    /**
     * 获取当前用户状态
     */
    fun getStatus(): StatusCode {
        return NIMClient.getStatus()
    }
}