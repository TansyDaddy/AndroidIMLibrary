package com.renyu.nimlibrary.manager

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.*
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.extension.CustomAttachParser
import com.renyu.nimlibrary.util.RxBus
import java.io.File


object MessageManager {

    /**
     * 注册自定义消息类型解析
     */
    fun registerCustomAttachmentParser() {
        NIMClient.getService(MsgService::class.java).registerCustomAttachmentParser(CustomAttachParser())
    }

    /**
     * 监听新消息接收
     */
    fun observeReceiveMessage() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage({
                    Log.d("NIM_APP", "发生observeReceiveMessage回调")
                    if (it != null) {
                        it.filter {
                            it.fromAccount != null
                        }.forEach {
                            Log.d("NIM_APP", "收到新消息:${it.fromNick}")
                        }
                        RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.ReceiveMessage))
                    }
                }, true)
    }

    /**
     * 监听最近会话变更
     */
    fun observeRecentContact() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRecentContact({
                    Log.d("NIM_APP", "发生observeRecentContact回调")
                    if (it != null) {
                        it.filter {
                            it.fromAccount != null
                        }.forEach {
                            Log.d("NIM_APP", "最近会话列表变更:${it.fromNick}")
                        }
                        RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.ObserveRecentContact))
                    }
                }, true)
    }

    /**
     * 监听最近联系人被删除
     */
    fun observeRecentContactDeleted() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRecentContactDeleted({ t -> Log.d("NIM_APP", "最近会话列表变更${t?.contactId}") }, true)
    }

    /**
     * 监听消息状态
     */
    fun observeMsgStatus() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeMsgStatus({
                    RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.MsgStatus))
                    Log.d("NIM_APP", "消息状态：${it.fromNick} ${it.uuid} ${it.status}")
                }, true)
    }

    /**
     * 监听消息撤回
     */
    fun observeRevokeMessage() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRevokeMessage({
                    if (it?.message == null) {
                        return@observeRevokeMessage
                    }
                    RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.RevokeMessage))
                    Log.d("NIM_APP", "被撤回的消息消息ID：${it.message.uuid}")
                }, true)
    }

    /**
     * 监听消息已读回执
     */
    fun observeMessageReceipt() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeMessageReceipt({
                    RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.MessageReceipt))
                    it.forEach {
                        Log.d("NIM_APP", "已读回执消息回执ID：${it.sessionId} ${it.time}")
                    }
                }, true)
    }

    /**
     * 监听消息附件上传/下载进度
     */
    fun observeAttachmentProgress() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeAttachmentProgress({
                    Log.d("NIM_APP", "上传/下载进度消息ID：${it.uuid} ${it.transferred*100/it.total}")
                }, true)
    }

    /**
     * 监听自定义通知
     */
    fun observeCustomNotification() {
        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification({
            RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.CustomNotification))
            Log.d("NIM_APP", "收到自定义通知：${it?.content}")
        }, true)
    }

    /**
     * 将所有联系人的未读数清零
     */
    fun clearAllUnreadCount() {
        NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
    }

    /**
     * 清除与指定用户的所有消息记录
     */
    fun clearChattingHistory(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).clearChattingHistory(account, sessionType)
    }

    /**
     * 清空消息数据库的所有消息记录
     */
    fun clearMsgDatabase() {
        NIMClient.getService(MsgService::class.java).clearMsgDatabase(true)
    }

    /**
     * 将指定最近联系人的未读数清零
     */
    fun clearUnreadCount(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).clearUnreadCount(account, sessionType)
    }

    /**
     * 删除一条消息记录
     */
    fun deleteChattingHistory(imMessage: IMMessage) {
        NIMClient.getService(MsgService::class.java).deleteChattingHistory(imMessage)
    }

    /**
     * 从最近联系人列表中删除一项
     */
    fun deleteRecentContact(recent: RecentContact) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact(recent)
    }

    /**
     * 删除最近联系人记录
     */
    fun deleteRecentContact2(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(account, sessionType)
    }

    /**
     * 获取未读数总数
     */
    fun getTotalUnreadCount(): Int {
        return NIMClient.getService(MsgService::class.java).totalUnreadCount
    }

    /**
     * 获取最近会话列表
     */
    fun queryRecentContacts(requestCallback: RequestCallback<List<RecentContact>>) {
        NIMClient.getService(MsgService::class.java)
                .queryRecentContacts()
                .setCallback(requestCallback)
    }

    /**
     * 向前获取会话详情
     */
    fun queryMessageListExBefore(imMessage: IMMessage, requestCallback: RequestCallback<List<IMMessage>>) {
        queryMessageListEx(imMessage, QueryDirectionEnum.QUERY_OLD, requestCallback)
    }

    private fun queryMessageListEx(imMessage: IMMessage, direction: QueryDirectionEnum, requestCallback: RequestCallback<List<IMMessage>>) {
        NIMClient.getService(MsgService::class.java)
                .queryMessageListEx(imMessage, direction, 20, true)
                .setCallback(requestCallback)
    }

    /**
     * 远程获取历史数据
     */
    fun pullMessageHistory(imMessage: IMMessage, requestCallback: RequestCallback<List<IMMessage>>) {
        NIMClient.getService(MsgService::class.java)
                .pullMessageHistory(imMessage, 20, true)
                .setCallback(requestCallback)
    }

    /**
     * 向后查找最新数据
     */
    fun pullMessageHistoryEx(imMessage: IMMessage, requestCallback: RequestCallback<List<IMMessage>>) {
        NIMClient.getService(MsgService::class.java)
                .pullMessageHistoryEx(imMessage, System.currentTimeMillis(), 100, QueryDirectionEnum.QUERY_NEW, true)
                .setCallback(requestCallback)
    }

    /**
     * 消息撤回
     */
    fun revokeMessage(imMessage: IMMessage, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).revokeMessage(imMessage).setCallback(requestCallback)
    }

    /**
     * 保存消息到本地数据库，但不发送到服务器端
     */
    fun saveMessageToLocal(imMessage: IMMessage, notify: Boolean, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).saveMessageToLocal(imMessage, notify).setCallback(requestCallback)
    }

    /**
     * 保存消息到本地数据库，但不发送到服务器端
     */
    fun saveMessageToLocalEx(imMessage: IMMessage, notify: Boolean, time: Long, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).saveMessageToLocalEx(imMessage, notify, time).setCallback(requestCallback)
    }

    /**
     * 设置当前正在聊天的对象。设置后会影响内建的消息提醒。如果有新消息到达，且消息来源是正在聊天的对象，将不会有消息提醒。
     * 调用该接口还会附带调用clearUnreadCount(String, SessionTypeEnum),将正在聊天对象的未读数清零。
     */
    fun setChattingAccount(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).setChattingAccount(account, sessionType)
    }

    /**
     * 发送文字消息
     */
    fun sendTextMessage(account: String, text: String): IMMessage {
        val message = MessageBuilder.createTextMessage(account, SessionTypeEnum.P2P, text)
        sendMessage(message, false)
        return message
    }

    /**
     * 发送图片消息
     */
    fun sendImageMessage(account: String, file: File): IMMessage {
        val message = MessageBuilder.createImageMessage(account, SessionTypeEnum.P2P, file, file.name)
        sendMessage(message, false)
        return message
    }

    /**
     * 发送文件消息
     */
    fun sendFileMessage(account: String, file: File): IMMessage {
        val message = MessageBuilder.createFileMessage(account, SessionTypeEnum.P2P, file, file.name)
        sendMessage(message, false)
        return message
    }

    /**
     * 发送音频消息
     */
    fun sendAudioMessage(account: String, file: File, duration: Long): IMMessage {
        val message = MessageBuilder.createAudioMessage(account, SessionTypeEnum.P2P, file, duration)
        sendMessage(message, false)
        return message
    }

    /**
     * 发送视频消息
     */
    fun sendVideoMessage(account: String, file: File): IMMessage {
        val mediaPlayer = MediaPlayer.create(Utils.getApp(), Uri.fromFile(file))
        val duration = mediaPlayer.duration.toLong()
        val height = mediaPlayer.videoHeight
        val width = mediaPlayer.videoWidth
        val message = MessageBuilder.createVideoMessage(account, SessionTypeEnum.P2P, file, duration, width, height, file.name)
        sendMessage(message, false)
        return message
    }

    /**
     * 发送位置消息
     */
    fun sendAudioMessage(account: String, lat: Double, lng: Double, addr: String): IMMessage {
        val message = MessageBuilder.createLocationMessage(account, SessionTypeEnum.P2P, lat, lng, addr)
        sendMessage(message, false)
        return message
    }

    /**
     * 生成提示消息
     */
    fun sendTipMessage(account: String, content: String) {
        val imMessage = MessageBuilder.createTipMessage(account, SessionTypeEnum.P2P)
        imMessage.content = content
        imMessage.status = MsgStatusEnum.success
        val config = CustomMessageConfig()
        config.enableUnreadCount = false
        imMessage.config = config
        NIMClient.getService(MsgService::class.java).saveMessageToLocal(imMessage, true)
    }

    /**
     * 消息撤回
     */
    fun sendRevokeMessage(imMessage: IMMessage, content: String) {
        val message = MessageBuilder.createTipMessage(imMessage.sessionId, imMessage.sessionType)
        message.content = content
        message.status = MsgStatusEnum.success
        val config = CustomMessageConfig()
        config.enableUnreadCount = false
        message.config = config
        NIMClient.getService(MsgService::class.java).saveMessageToLocalEx(message, true, imMessage.time)
    }

    /**
     * 发送自定义Message
     */
    fun createCustomMessage(account: String, content: String, attachment: MsgAttachment): IMMessage {
        val message = MessageBuilder.createCustomMessage(account, SessionTypeEnum.P2P, content, attachment)
        sendMessage(message, false)
        return message
    }

    /**
     * 重发消息
     */
    fun reSendMessage(imMessage: IMMessage) {
        sendMessage(imMessage, true)
    }

    /**
     * 发送一条指令消息
     */
    fun sendCustomNotification(command: CustomNotification) {
        NIMClient.getService(MsgService::class.java).sendCustomNotification(command)
    }

    private fun sendMessage(imMessage: IMMessage, resend: Boolean) {
        NIMClient.getService(MsgService::class.java).sendMessage(imMessage, resend)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        Log.d("NIM_APP", "消息发送成功")
                    }

                    override fun onFailed(code: Int) {
                        Log.d("NIM_APP", "消息发送失败 $code")
                        // 被接收方加入黑名单
                        if (code == ResponseCode.RES_IN_BLACK_LIST.toInt()) {
                            val tip = MessageBuilder.createTipMessage(imMessage.sessionId, SessionTypeEnum.P2P)
                            tip.content = "消息已发送，但对方拒收"
                            tip.status = MsgStatusEnum.success
                            val config = CustomMessageConfig()
                            config.enableUnreadCount = false
                            tip.config = config
                            // 防止消息时间相同造成时间小时错误，故加1ms
                            NIMClient.getService(MsgService::class.java).saveMessageToLocalEx(tip, true, imMessage.time + 1)
                        }
                    }

                    override fun onException(exception: Throwable?) {
                        Log.d("NIM_APP", "消息发送失败 ${exception?.message}")
                    }
                })
    }

    /**
     * 发送P2P消息已读回执
     */
    fun sendReceipt(account: String, imMessage: IMMessage) {
        NIMClient.getService(MsgService::class.java)
                .sendMessageReceipt(account, imMessage)
                .setCallback(object : RequestCallbackWrapper<Void>() {
            override fun onResult(code: Int, result: Void?, exception: Throwable?) {
                if (200 == code) {
                    Log.d("NIM_APP", "消息回执发送成功")
                }
            }
        })
    }

    /**
     * 附件下载失败，重新下载附件
     */
    fun downloadAttachment(imMessage: IMMessage, requestCallback: RequestCallbackWrapper<Void>?) {
        NIMClient.getService(MsgService::class.java)
                .downloadAttachment(imMessage, false)
                .setCallback(requestCallback)
    }

    /**
     * 更新消息
     */
    fun updateIMMessageStatus(imMessage: IMMessage) {
        NIMClient.getService(MsgService::class.java).updateIMMessageStatus(imMessage)
    }

    /**
     * 是否需要通知栏通知
     */
    fun enableMsgNotification(enable: Boolean) {
        if (enable) {
            NIMClient.getService(MsgService::class.java).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None)
        }
        else {
            NIMClient.getService(MsgService::class.java).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None)
        }
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    fun registerIMMessageFilter() {
        NIMClient.getService(MsgService::class.java).registerIMMessageFilter {
            if (it.attachment is AVChatAttachment) {
                return@registerIMMessageFilter true
            }
            return@registerIMMessageFilter false
        }
    }
}