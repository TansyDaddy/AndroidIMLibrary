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
        val notificationActivityName = clazz.getField("NotificationActivityName").get(clazz).toString()
        val notificationActivityClass = Class.forName(notificationActivityName)

        // 点击通知需要跳转到的界面
        config.notificationEntrance = notificationActivityClass as Class<out Activity>
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
        config.hwCertificateName = clazz.getField("hwCertificateName").get(clazz).toString()

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
        val account = getUserAccount().first
        if (!TextUtils.isEmpty(account)) {
            NIMClient.getService(AuthService::class.java).openLocalCache(account)
        }
    }

    /**
     * 配置基础监听
     */
    fun initObserve() {
        // 监听用户在线状态
        StatueManager.observeOnlineStatus()
        // 监听数据同步状态
        StatueManager.observeLoginSyncDataStatus()
        // 监听多端登录状态
        StatueManager.observeOtherClients()
        // 过滤音视频聊天消息
        MessageManager.registerIMMessageFilter()
        // 消息接收观察者
        MessageManager.observeReceiveMessage()
        // 监听最近会话变更
        MessageManager.observeRecentContact()
        // 监听消息状态
        MessageManager.observeMsgStatus()
        // 监听最近联系人被删除
        MessageManager.observeRecentContactDeleted()
        // 监听消息撤回
        MessageManager.observeRevokeMessage()
        // 监听消息已读回执
        MessageManager.observeMessageReceipt()
        // 监听消息附件上传/下载进度
        MessageManager.observeAttachmentProgress()
        // 监听自定义通知
        MessageManager.observeCustomNotification()
        // 监听用户资料变更
        UserManager.observeUserInfoUpdate()
        // 监听黑名单变更通知
        FriendManager.observeBlackListChangedNotify()
        // 监听好友关系变化通知
        FriendManager.observeFriendChangedNotify()
        // 注册自定义消息类型解析
        MessageManager.registerCustomAttachmentParser()
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

    /**
     * 判断是不是登录成功
     */
    fun isLogined(): Boolean {
        return getStatus() == StatusCode.LOGINED
    }

    /**
     * 登出
     */
    fun logout() {
        // 清除用户登录信息
        setUserAccount(null, null)

        NIMClient.getService(AuthService::class.java).logout()
    }

    /**
     * 设置用户信息
     */
    fun setUserAccount(accid: String?, token: String?) {
        if (TextUtils.isEmpty(accid) || TextUtils.isEmpty(token)) {
            // 清除用户登录信息
            SPUtils.getInstance().remove(CommonParams.SP_ACCID)
            SPUtils.getInstance().remove(CommonParams.SP_TOKEN)
        }
        else {
            SPUtils.getInstance().put(CommonParams.SP_ACCID, accid)
            SPUtils.getInstance().put(CommonParams.SP_TOKEN, token)
        }
    }

    /**
     * 获取用户信息
     */
    fun getUserAccount(): Pair<String, String> {
        val accid = SPUtils.getInstance().getString(CommonParams.SP_ACCID)
        val token = SPUtils.getInstance().getString(CommonParams.SP_TOKEN)
        return Pair(accid, token)
    }
}