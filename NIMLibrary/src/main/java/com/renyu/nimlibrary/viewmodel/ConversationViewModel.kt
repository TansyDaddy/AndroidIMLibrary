package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification
import com.renyu.nimavchatlibrary.manager.BaseAVManager
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum
import com.renyu.nimavchatlibrary.ui.InComingAVChatActivity
import com.renyu.nimavchatlibrary.ui.OutGoingAVChatActivity
import com.renyu.nimlibrary.bean.*
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.extension.StickerAttachment
import com.renyu.nimlibrary.extension.VRAttachment
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.activity.MapPreviewActivity
import com.renyu.nimlibrary.ui.adapter.ConversationAdapter
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import com.renyu.nimlibrary.util.ClipboardUtils
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ConversationViewModel(private val account: String, private val sessionType: SessionTypeEnum) : ViewModel(), EventImpl {

    private val messages: ArrayList<IMMessage> by lazy {
        ArrayList<IMMessage>()
    }

    // 接口请求数据
    private val messageListReqeuestLocal: MutableLiveData<IMMessage> = MutableLiveData()
    var messageListResponseLocal: LiveData<Resource<List<IMMessage>>>? = null
    private val messageListReqeuestRemote: MutableLiveData<IMMessage> = MutableLiveData()
    var messageListResponseRemote: LiveData<Resource<List<IMMessage>>>? = null

    val adapter: ConversationAdapter by lazy {
        ConversationAdapter(messages, this)
    }

    // "正在输入提示"指令发送时间间隔
    private var typingTime: Long = 0

    // 是不是正在同步远程数据
    private var isAsync = false

    init {
        messageListResponseLocal = Transformations.switchMap(messageListReqeuestLocal) {
            if (it == null) {
                MutableLiveData<Resource<List<IMMessage>>>()
            }
            else {
                Repos.queryMessageListExLocal(it)
            }
        }

        messageListResponseRemote = Transformations.switchMap(messageListReqeuestRemote) {
            if (it == null) {
                MutableLiveData<Resource<List<IMMessage>>>()
            }
            else {
                Repos.pullMessageHistory(it)
            }
        }
    }

    /**
     * 每个页面都要判断登录状态
     */
    fun signIn() {
        if (AuthManager.getStatus() != StatusCode.LOGINED && NetworkUtils.isConnected()) {
            AuthManager.login(UserManager.getUserAccount().first,
                    UserManager.getUserAccount().second)
        }
    }

    /**
     * 获取会话详情列表
     */
    fun queryMessageLists(imMessage: IMMessage?) {
        var temp = imMessage
        if (imMessage == null) {
            temp = MessageBuilder.createEmptyMessage(account, sessionType, 0)
        }
        messageListReqeuestLocal.value = temp
    }

    /**
     * 加载更多消息或同步消息
     */
    fun pullMessageHistory(isFirst: Boolean) {
        val temp = if (isFirst) {
            MessageBuilder.createEmptyMessage(account, sessionType, 0)
        }
        else {
            messages[0]
        }
        messageListReqeuestRemote.value = temp
    }

    /**
     * 消息同步比较
     */
    fun compareData(remoteMessages: List<IMMessage>?) {
        if (remoteMessages == null) {
            return
        }
        // 开启同步
        isAsync = true
        // 第一条产生差别的消息索引位置
        var notFindIndex = -1
        for ((withIndex, value) in remoteMessages.withIndex()) {
            var find = false
            // 循环从新到旧查找本地，跳过本地自己保存的消息
            loop@
            for (temp in messages) {
                if (temp.uuid == value.uuid) {
                    find = true
                    break@loop
                }
            }
            // 没有找到
            if (!find) {
                notFindIndex = withIndex
                break
            }
        }

        if (notFindIndex != -1) {
            // 找到最后一条相同的消息
            val lastSameIndex = notFindIndex - 1
            // 一开始消息就不同
            if (lastSameIndex == -1) {
                messages.clear()
            }
            else {
                val lastSameMessage = remoteMessages[lastSameIndex]
                // 从最后一条相同的消息开始，去除时间比之早的消息
                val temp: ArrayList<IMMessage> = ArrayList()
                messages.filter {
                    it.time < lastSameMessage.time
                }.forEach {
                    temp.add(it)
                }
                messages.removeAll(temp)
            }
            // 补足之后的消息
            var temp2: ArrayList<IMMessage> = ArrayList()
            for (i in notFindIndex until remoteMessages.size) {
                temp2.add(remoteMessages[i])
            }
            // 倒序排列
            Collections.reverse(temp2)
            // 添加同步过来的额外新数据
            messages.addAll(0, temp2)

            adapter.updateShowTimeItem(messages, true, true)
            adapter.notifyDataSetChanged()
        }
        // 同步结束
        isAsync = false
    }

    /**
     * 添加更多历史消息数据
     */
    fun addOldIMMessages(imMessages: List<IMMessage>, isLoadmore: Boolean) {
        if (imMessages.isEmpty()) {
            return
        }
        // 不是添加下拉获取到的更多消息，则直接清空
        if (!isLoadmore) {
            messages.clear()
        }
        messages.addAll(0, imMessages)
        adapter.updateShowTimeItem(messages, true, !isLoadmore)
        // 添加新数据
        adapter.notifyItemRangeInserted(0, imMessages.size)
        // 根据时间变化刷新之前的列表
        adapter.notifyItemRangeChanged(imMessages.size, messages.size)
    }

    /**
     * 添加新消息数据
     */
    fun addNewIMMessages(observeResponse: ObserveResponse): Boolean {
        if (isAsync) {
            return false
        }
        val temp = ArrayList<IMMessage>()
        for (message in observeResponse.data as List<*>) {
            // 判断消息是否属于当前会话并且是否已经添加
            if (message is IMMessage && isMyMessage(message) && !isMessageAdded(message)) {
                temp.add(message)
            }
        }
        // 添加消息并排序
        messages.addAll(temp)
        sortMessages(messages)
        adapter.updateShowTimeItem(messages, false, true)
        adapter.notifyDataSetChanged()
        return true
    }

    /**
     * 消息发送后同步会话列表
     */
    fun refreshSendIMMessage(vararg imMessages: IMMessage) {
        messages.addAll(imMessages)
        adapter.updateShowTimeItem(messages, false, true)
        adapter.notifyItemInserted(messages.size - 1)
    }

    /**
     * 重新发送消息
     */
    override fun resendIMMessage(view: View, uuid: String) {
        super.resendIMMessage(view, uuid)
        if (isAsync) {
            return
        }
        // 找到重发的那条消息
        val imMessages = messages.filter {
            it.uuid == uuid
        }.take(1)
        if (imMessages.isNotEmpty()) {
            val imMessage = imMessages[0]
            imMessage.status = MsgStatusEnum.sending
            // 删除之前的数据
            deleteItem(imMessage, true)
            // 添加为新的数据
            messages.add(imMessage)
            // 重新调整时间
            adapter.updateShowTimeItem(messages, false, true)
            adapter.notifyDataSetChanged()

            MessageManager.reSendMessage(imMessage)
        }
    }

    /**
     * 删除消息
     */
    fun deleteIMMessage(imMessage: IMMessage) {
        val index = deleteItem(imMessage, true)
        adapter.notifyItemRemoved(index)
        // 重新调整时间
        adapter.updateShowTimeItem(messages, true, false)
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新消息状态
     */
    fun updateIMMessage(observeResponse: ObserveResponse) {
        val imMessage = observeResponse.data as IMMessage
        // 遍历找到并刷新
        for ((index, message) in messages.withIndex()) {
            if (message.uuid == imMessage.uuid) {
                // 网易云信这里有bug，如果在当前页面发送完消息（比如图片），发送状态会更新；如果在发送过程中离开页面再进来，则会出现发送状态不更新的情况
                message.status = imMessage.status
                Handler().postDelayed({
                    adapter.notifyItemChanged(index)
                }, 250)
            }
        }
    }

    /**
     * 更新VR卡片当前状态
     */
    fun updateVRCardStatus(aVChatTypeEnum: AVChatTypeEnum) {
        // 遍历找到并刷新
        for ((index, message) in messages.withIndex()) {
            if (message.uuid == CommonParams.currentVRUUID) {
                CommonParams.currentVRStatus = aVChatTypeEnum
                adapter.notifyItemChanged(index)
            }
        }
    }

    /**
     * 长按消息列表
     */
    override fun onLongClick(view: View, imMessage: IMMessage): Boolean {
        (view.context as ConversationFragment.ConversationListener).longClick(view, imMessage, messages.indexOf(imMessage))
        return super.onLongClick(view, imMessage)
    }

    /**
     * 判断是不是当前聊天用户的消息
     */
    private fun isMyMessage(imMessage: IMMessage): Boolean {
        return imMessage.sessionType == sessionType && imMessage.sessionId != null && imMessage.sessionId == account
    }

    /**
     * 判断是不是已经添加过的消息
     */
    private fun isMessageAdded(imMessage: IMMessage): Boolean {
        var isAdded = false
        messages.filter {
            it.uuid == imMessage.uuid
        }.forEach {
            isAdded = true
        }
        return isAdded
    }

    /**
     * 删除消息
     */
    private fun deleteItem(imMessage: IMMessage, isRelocateTime: Boolean): Int {
        MessageManager.deleteChattingHistory(imMessage)
        var index = 0
        for (item in messages) {
            if (item.isTheSame(imMessage)) {
                break
            }
            ++index
        }
        if (index < messages.size) {
            messages.removeAt(index)
            if (isRelocateTime) {
                adapter.relocateShowTimeItemAfterDelete(imMessage, index, messages)
            }
        }
        return index
    }

    /**
     * 消息主动撤回
     */
    fun sendRevokeIMMessage(imMessage: IMMessage) {
        MessageManager.revokeMessage(imMessage, object : RequestCallback<Void> {
            override fun onSuccess(param: Void?) {
                deleteItem(imMessage, false)
                val revokeNick = if (imMessage.fromAccount == UserManager.getUserAccount().first) "你" else "对方"
                MessageManager.sendRevokeMessage(imMessage, revokeNick + "撤回了一条消息")
            }

            override fun onFailed(code: Int) {
                if (code == ResponseCode.RES_OVERDUE.toInt()) {
                    Toast.makeText(Utils.getApp(), "发送时间超过2分钟的消息，不能被撤回", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(Utils.getApp(), "消息撤回出错", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onException(exception: Throwable?) {

            }
        })
    }

    /**
     * 对方撤回消息
     */
    fun receiverRevokeMessage(notification: RevokeMsgNotification) {
        if (notification.message.sessionId == account) {
            deleteItem(notification.message, false)
            MessageManager.sendRevokeMessage(notification.message, "对方撤回了一条消息")
        }
    }

    /**
     * 复制文字消息
     */
    fun copyIMMessage(imMessage: IMMessage) {
        ClipboardUtils.copyText(imMessage.content)
    }

    /**
     * 判断是否需要发送完消息之后补充发送用户信息卡片
     */
    fun isSendUserInfoAfterSend(imMessage: IMMessage, need: Boolean, extraMessage: String) {
        if (need) {
            // 判断上一条用户发出的消息的类型
            val lastMessage = when(imMessage.msgType) {
                MsgTypeEnum.text -> {
                    // 如果是文本消息，则去除文字即可
                    imMessage.content
                }
                MsgTypeEnum.image -> {
                    // 如果是图片消息，则使用固定文字
                    "[图片]"
                }
                MsgTypeEnum.audio -> {
                    // 如果是语音消息，则使用固定文字
                    "[语音消息]"
                }
                MsgTypeEnum.location -> {
                    // 如果是位置消息，则使用固定文字
                    "[位置]"
                }
                MsgTypeEnum.custom -> {
                    "[自定义卡片消息，待完善]"
                }
                else -> ""
            }
            val temp = MessageManager.sendUserInfoMessage(account, UserInfoItem(extraMessage, lastMessage), "用户信息")
            refreshSendIMMessage(imMessage, temp)
        }
        else {
            refreshSendIMMessage(imMessage)
        }
    }

    /**
     * 添加临时提示消息
     */
    fun addTempHappyMessage(account: String, tip: String) {
        messages.add(MessageManager.addTempTipMessage(account, tip))
        adapter.notifyItemInserted(messages.size - 1)
    }

    /**
     * 客户前往VR去电页面
     */
    override fun gotoVrOutgoingCall(view: View, imMessage: IMMessage) {
        super.gotoVrOutgoingCall(view, imMessage)
        // 只有当前发送的卡片才能重复点击
        if (CommonParams.currentVRUUID == imMessage.uuid) {
            // 客户进入VR环节
            OutGoingAVChatActivity.outgoingCall(Utils.getApp(), imMessage.sessionId, CommonParams.currentVRUUID, true)
        }
    }

    /**
     * 经纪人前往VR来电页面
     */
    override fun gotoVrInComingCall(view: View, imMessage: IMMessage, receive: Boolean) {
        super.gotoVrInComingCall(view, imMessage, receive)
        // 若收到音频被叫且主叫人为当前页面聊天的对象，客户触发的卡片与经纪人点击的卡片相同，则判断通过
        if (BaseAVManager.avChatData != null &&
                BaseAVManager.avChatData.account == account &&
                BaseAVManager.avChatData.extra == imMessage.uuid) {
            InComingAVChatActivity.incomingCall(Utils.getApp(), imMessage.sessionId, (imMessage.attachment as VRAttachment).vrJson, receive)
        }
    }

    /**
     * 前往地图预览页面
     */
    override fun gotoMapPreview(view: View, imMessage: IMMessage) {
        super.gotoMapPreview(view, imMessage)
        val attachment = imMessage.attachment as LocationAttachment
        val intent = Intent(Utils.getApp(), MapPreviewActivity::class.java)
        intent.putExtra("address", attachment.address)
        intent.putExtra("lat", attachment.latitude)
        intent.putExtra("lng", attachment.longitude)
        Utils.getApp().startActivity(intent)
    }

    /**
     * 前往个人详情
     */
    override fun gotoUserInfo(view: View, account: String) {
        super.gotoUserInfo(view, account)
        (view.context as ConversationFragment.ConversationListener).gotoUserInfo(account)
    }

    /**
     * 打开大图
     */
    override fun openBigImageViewActivity(view: View) {
        super.openBigImageViewActivity(view)

        val temp = ArrayList<String>()
        var index = -1
        messages.filter {
            it.attachment is ImageAttachment
        }.forEach {
            val imageAttachment = it.attachment as ImageAttachment
            if (imageAttachment.path != null) {
                val file = File(imageAttachment.path)
                if (file.exists()) {
                    temp.add(imageAttachment.path)
                    index++
                }
            }
            else {
                temp.add(imageAttachment.url)
                index++
            }
        }
        (view.context as ConversationFragment.ConversationListener).showBigImage(temp, index)
    }

    /**
     * 打开房源卡片
     */
    override fun openHouseCard(view: View, imMessage: IMMessage) {
        super.openHouseCard(view, imMessage)
        (view.context as ConversationFragment.ConversationListener).openHouseCard(imMessage)
    }

    /**
     * 收到已读回执
     */
    fun receiverMsgReceipt() {
        adapter.notifyDataSetChanged()
    }

    /**
     * 发送已读回执
     */
    fun sendMsgReceipt() {
        if (sessionType !== SessionTypeEnum.P2P) {
            return
        }
        val message = getLastReceivedMessage()
        if (message != null) {
            MessageManager.sendReceipt(account, message)
        }
    }

    /**
     * 得到最后一条要发送回执的信息
     */
    private fun getLastReceivedMessage(): IMMessage? {
        var lastMessage: IMMessage? = null
        for (i in messages.indices.reversed()) {
            if (sendReceiptCheck(messages[i])) {
                lastMessage = messages[i]
                break
            }
        }
        return lastMessage
    }

    /**
     * 非收到的消息，Tip消息和通知类消息，不要发已读回执
     */
    private fun sendReceiptCheck(imMessage: IMMessage): Boolean {
        return !(imMessage.direct != MsgDirectionEnum.In ||
                imMessage.msgType == MsgTypeEnum.tip ||
                imMessage.msgType == MsgTypeEnum.notification)
    }

    /**
     * 准备文字消息
     */
    fun prepareText(text: String): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendTextMessage(account, text)
    }

    /**
     * 准备图片消息
     */
    fun prepareImageFile(file: File): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendImageMessage(account, file)
    }

    /**
     * 准备语音消息
     */
    fun prepareAudio(file: File, duration: Long): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendAudioMessage(account, file, duration)
    }

    /**
     * 准备地理位置消息
     */
    fun prepareLocation(latLng: LatLng, address: String): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendLocationMessage(account, latLng.latitude, latLng.longitude, address)
    }

    /**
     * 准备贴图消息
     */
    fun prepareSticker(stickerItem: StickerItem): IMMessage? {
        if (isAsync) {
            return null
        }
        val attachment = StickerAttachment(stickerItem.category, stickerItem.name)
        return MessageManager.sendCustomMessage(account, "贴图消息", attachment)
    }

    /**
     * 准备楼盘卡片消息
     */
    fun prepareHouseCard(houseItem: HouseItem): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendHouseCardMessage(account, houseItem, houseItem.houseJson)
    }

    /**
     * 准备VR卡片消息
     */
    fun prepareVRCard(vrItem: VRItem): IMMessage? {
        if (isAsync) {
            return null
        }
        return MessageManager.sendVRCardMessage(account, vrItem, vrItem.vrJson)
    }

    /**
     * 发送“正在输入”通知
     */
    fun sendTypingCommand() {
        // 每5s发出一次
        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis()
            val command = CustomNotification()
            command.sessionId = account
            command.sessionType = sessionType
            val config = CustomNotificationConfig()
            config.enablePush = false
            config.enableUnreadCount = false
            command.config = config
            val json = JSONObject()
            json.put(CommonParams.TYPE, CommonParams.COMMAND_INPUT)
            command.content = json.toString()
            MessageManager.sendCustomNotification(command)
        }
    }

    /**
     * 消息排序
     */
    private fun sortMessages(list: List<IMMessage>) {
        if (!list.isEmpty()) {
            Collections.sort(list, comp)
        }
    }

    private val comp = Comparator<IMMessage> { o1, o2 ->
        val time = o1!!.time - o2!!.time
        if (time == 0L) 0 else if (time < 0) -1 else 1
    }
}