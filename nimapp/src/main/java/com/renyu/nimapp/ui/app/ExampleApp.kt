package com.renyu.nimapp.ui.app

import android.support.multidex.MultiDexApplication
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco
import com.netease.nimlib.sdk.util.NIMUtil
import com.renyu.nimavchatlibrary.noui.manager.AVManager
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.StatueManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams

class ExampleApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // 初始化工具库
        Utils.init(this)
        // 初始化Fresco
        Fresco.initialize(this)

        // 初始化云信
        AuthManager.init(CommonParams.SDKROOT, "house365")

        if (NIMUtil.isMainProcess(this)) {
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
            // 注册自定义消息类型解析
            MessageManager.registerCustomAttachmentParser()
            // 音视频通话接听配置
            AVManager.observeIncomingCall()

            // 假登录获取本地数据
            AuthManager.fakeLogin()
        }
    }
}