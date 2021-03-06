package com.renyu.nimlibrary.ui.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.*
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.databinding.FragmentConversationBinding
import com.renyu.nimlibrary.extension.UserInfoAttachment
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.activity.MapActivity
import com.renyu.nimlibrary.ui.view.WrapContentLinearLayoutManager
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.util.audio.MessageAudioControl
import com.renyu.nimlibrary.util.sticker.StickerUtils
import com.renyu.nimlibrary.viewmodel.ConversationViewModel
import com.renyu.nimlibrary.viewmodel.ConversationViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_conversation.*
import kotlinx.android.synthetic.main.nim_message_activity_text_layout.*
import kotlinx.android.synthetic.main.panel_content.*
import kotlinx.android.synthetic.main.panel_emoji.*
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Administrator on 2018/7/30.
 */
class ConversationFragment : Fragment(), EventImpl {

    // 进入会话详情的类型
    enum class CONVERSATIONTYPE {
        // VR带看
        VR,
        // 用户主动发送一条信息后触发发送用户信息
        SendUserInfoAfterSend,
        // 每一个新的聊天对象触发一次发送楼盘卡片
        SendOneTime,
        // 带提示的消息
        TIP,
        // 默认场景
        UNSPECIFIED
    }

    // 卡片类型
    enum class ConversationCard {
        ALUMNI,
        CAMERA,
        HOUSE,
        LOCATION,
        EVALUATE,
        TIPOFFS
    }

    companion object {
        // 当前发送的VR卡片的UUID
        var currentVRUUID = ""
        // 当前发送的VR卡片的状态
        var currentVRStatus = AVChatTypeEnum.VALID

        /**
         * 直接打开会话详情
         */
        fun getInstance(account: String, isGroup: Boolean, cards: Array<ConversationCard>): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString(CommonParams.ACCOUNT, account)
            bundle.putSerializable(CommonParams.TYPE, CONVERSATIONTYPE.UNSPECIFIED)
            bundle.putBoolean(CommonParams.ISGROUP, isGroup)
            bundle.putSerializable(CommonParams.CARD, cards)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * 用户主动发送一条信息后触发发送用户信息
         */
        fun getInstanceWithSendUserInfoAfterSend(account: String, userInfo: String, isGroup: Boolean, cards: Array<ConversationCard>, tip: String): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString(CommonParams.ACCOUNT, account)
            bundle.putSerializable(CommonParams.TYPE, CONVERSATIONTYPE.SendUserInfoAfterSend)
            bundle.putString(CommonParams.USERINFO, userInfo)
            bundle.putBoolean(CommonParams.ISGROUP, isGroup)
            bundle.putSerializable(CommonParams.CARD, cards)
            bundle.putString(CommonParams.TIP, tip)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * 用户主动发送一条信息后触发发送用户信息
         */
        fun getInstanceWithSendOneTime(account: String, houseItem: HouseItem, isGroup: Boolean, cards: Array<ConversationCard>, tip: String): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString(CommonParams.ACCOUNT, account)
            bundle.putSerializable(CommonParams.TYPE, CONVERSATIONTYPE.SendOneTime)
            bundle.putSerializable(CommonParams.HOUSEITEM, houseItem)
            bundle.putBoolean(CommonParams.ISGROUP, isGroup)
            bundle.putSerializable(CommonParams.CARD, cards)
            bundle.putString(CommonParams.TIP, tip)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * 带提示的消息
         */
        fun getInstanceWithTip(account: String, isGroup: Boolean, cards: Array<ConversationCard>, tip: String): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString(CommonParams.ACCOUNT, account)
            bundle.putSerializable(CommonParams.TYPE, CONVERSATIONTYPE.TIP)
            bundle.putBoolean(CommonParams.ISGROUP, isGroup)
            bundle.putSerializable(CommonParams.CARD, cards)
            bundle.putString(CommonParams.TIP, tip)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * 发送VR卡片后打开详情
         */
        fun getInstanceWithVRCard(account: String, vrItem: VRItem, isGroup: Boolean, cards: Array<ConversationCard>, tip: String): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString(CommonParams.ACCOUNT, account)
            bundle.putSerializable(CommonParams.TYPE, CONVERSATIONTYPE.VR)
            bundle.putSerializable(CommonParams.VRITEM, vrItem)
            bundle.putBoolean(CommonParams.ISGROUP, isGroup)
            bundle.putSerializable(CommonParams.CARD, cards)
            bundle.putString(CommonParams.TIP, tip)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var viewDataBinding: FragmentConversationBinding? = null

    var vm: ConversationViewModel? = null

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    // 面板是否已经收起
    private var isExecuteCollapse = false

    // "正在输入"提示刷新使用
    val titleChangeHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }
    private val titleChangeRunnable: Runnable = Runnable { conversationListener?.titleChange(true) }

    private var conversationListener: ConversationListener? = null
    // 使用到的相关接口
    interface ConversationListener {
        // "正在输入"
        fun titleChange(reset: Boolean)
        // 拍照
        fun takePhoto()
        // 选择相册
        fun pickPhoto()
        // 选择楼盘
        fun choiceHouse()
        // 评价
        fun evaluate()
        // 打开个人详情
        fun gotoUserInfo(account: String)
        // 浏览大图
        fun showBigImage(images: ArrayList<String>, index: Int)
        // 长按列表
        fun longClick(view: View, imMessage: IMMessage, choicePosition: Int)
        // 打开楼盘卡片
        fun openHouseCard(imMessage: IMMessage)
        // 举报
        fun tipOffs()
    }

    // 每一个新的聊天对象触发一次发送楼盘卡片是否已经发送完成
    var hasFinishSendOneTime = false
    // 用户主动发送一条信息后触发发送用户信息是否已经发送完成
    var hasFinishSendUserInfoAfterSend = false

    // 是否正在获取历史消息
    var isPullMessageHistory = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        conversationListener = context as ConversationListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            hasFinishSendOneTime = savedInstanceState.getBoolean("hasFinishSendOneTime")
            hasFinishSendUserInfoAfterSend = savedInstanceState.getBoolean("hasFinishSendUserInfoAfterSend")
        }

        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversation, container, false)
        return viewDataBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 处理最后一条VR卡片的状态
        if (UserManager.getUserAccount().third == UserManager.UserRole.AGENT) {
            ConversationFragment.currentVRStatus = AVChatTypeEnum.VALID
        }
        else if (UserManager.getUserAccount().third == UserManager.UserRole.CUSTOMER) {
            // 从VR带看页面进来
            ConversationFragment.currentVRStatus = if (arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.VR) {
                AVChatTypeEnum.VALID
            } else {
                AVChatTypeEnum.INVALID
            }
        }

        viewDataBinding.also {
            vm = ViewModelProviders.of(this,
                    ConversationViewModelFactory(arguments!!.getString(CommonParams.ACCOUNT),
                            if (arguments!!.getBoolean(CommonParams.ISGROUP)) SessionTypeEnum.Team else SessionTypeEnum.P2P))
                    .get(ConversationViewModel::class.java)
            vm!!.messageListResponseLocal?.observe(this, Observer {
                when(it?.status) {
                    Status.SUCESS -> {
                        vm!!.addOldIMMessages(it.data!!, false)

                        // 用户已登录，在需要发送楼盘卡片的情况下执行发送卡片
                        if (!hasFinishSendOneTime && arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.SendOneTime) {
                            if (NIMClient.getStatus() == StatusCode.LOGINED) {
                                hasFinishSendOneTime = true
                                // 发送楼盘卡片
                                sendHousecardDirectly(arguments!!.getSerializable(CommonParams.HOUSEITEM) as HouseItem)
                                vm!!.addTempHappyMessage(arguments!!.getString(CommonParams.ACCOUNT), arguments!!.getString(CommonParams.TIP))
                            }
                        }
                        // 用户已登录，在需要发送VR卡片的情况下执行发送卡片
                        else if (!hasFinishSendOneTime && arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.VR) {
                            if (NIMClient.getStatus() == StatusCode.LOGINED) {
                                hasFinishSendOneTime = true
                                // 发送VR卡片
                                sendVRCard(arguments!!.getSerializable(CommonParams.VRITEM) as VRItem)
                                vm!!.addTempHappyMessage(arguments!!.getString(CommonParams.ACCOUNT), arguments!!.getString(CommonParams.TIP))
                            }
                        }
                        // 发送提示消息
                        else if (arguments!!.getSerializable(CommonParams.TYPE) != CONVERSATIONTYPE.UNSPECIFIED) {
                            vm!!.addTempHappyMessage(arguments!!.getString(CommonParams.ACCOUNT), arguments!!.getString(CommonParams.TIP))
                        }

                        // 首次加载完成滚动到最底部
                        rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)

                        // 加载远程数据进行同步
                        vm!!.pullMessageHistory(true)
                    }
                    Status.FAIL -> {

                    }
                    Status.LOADING -> {

                    }
                    Status.Exception -> {

                    }
                }
            })
            vm!!.messageListResponseRemote?.observe(this, Observer {

                // 获取历史数据完成
                isPullMessageHistory = false

                when(it?.status) {
                    Status.SUCESS -> {
                        // 首次刷新数据
                        if (it.message != null && it.message == "async") {
                            // 数据匹配检查
                            vm?.compareData(it.data!!)

                            // 首次加载完成发送消息已读回执
                            vm!!.sendMsgReceipt()

                            // 首次加载完成滚动到最底部
                            rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
                        }
                        else {
                            var temp = it.data!!
                            Collections.reverse(temp)
                            vm!!.addOldIMMessages(temp, true)
                            val linearManager = rv_conversation.layoutManager as WrapContentLinearLayoutManager
                            val firstItemPosition = linearManager.findFirstVisibleItemPosition()
                            if (firstItemPosition == 0) {
                                rv_conversation.scrollToPosition(it.data.size - 1)
                            }
                        }
                    }
                    Status.FAIL -> {
                        // 首次刷新数据
                        if (it.message != null && it.message == "async") {
                            vm?.compareData(null)
                        }
                    }
                    Status.LOADING -> {

                    }
                    Status.Exception -> {
                        // 首次刷新数据
                        if (it.message != null && it.message == "async") {
                            vm?.compareData(null)
                        }
                    }
                }
            })
            viewDataBinding!!.adapter = vm!!.adapter
            viewDataBinding!!.eventImpl = this

            initUI()

            // 添加基础监听
            disposable.add(RxBus.getDefault()
                    .toObservable(ObserveResponse::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        // 处理接收到的新消息
                        if (it.type == ObserveResponseType.ReceiveMessage) {
                            // 如果当前消息是最后一条的话就自动滚动到最底部
                            val isLast = isLastMessageVisible()
                            val receive = vm!!.addNewIMMessages(it)
                            // 正在同步中收到的新消息被忽略
                            if (!receive) {
                                return@doOnNext
                            }

                            // 如果是用户信息卡片，一定要滚动到最后
                            if ((it.data as List<*>).size == 1 &&
                                    (it.data as List<*>)[0] is IMMessage &&
                                    ((it.data as List<*>)[0] as IMMessage).msgType == MsgTypeEnum.custom &&
                                    ((it.data as List<*>)[0] as IMMessage).attachment is UserInfoAttachment) {
                                // 稍微延迟一点
                                Handler().postDelayed({
                                    smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
                                }, 350)
                            }
                            else if (isLast) {
                                smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
                            }

                            // 发送消息已读回执
                            vm!!.sendMsgReceipt()
                            // "正在输入"提示重置
                            titleChangeHandler.removeCallbacks(titleChangeRunnable)
                            conversationListener?.titleChange(true)
                        }
                        // 添加发出的消息状态监听
                        if (it.type == ObserveResponseType.MsgStatus) {
                            // 更新消息UI
                            vm!!.updateIMMessage(it)
                        }
                        // 对方消息撤回
                        if (it.type == ObserveResponseType.RevokeMessage) {
                            vm!!.receiverRevokeMessage(it.data as RevokeMsgNotification)
                        }
                        // 收到已读回执
                        if (it.type == ObserveResponseType.MessageReceipt) {
                            vm!!.receiverMsgReceipt()
                        }
                        // 消息同步完成
                        if (it.type == ObserveResponseType.ObserveLoginSyncDataStatus) {
                            // 数据同步完成之后在需要发送楼盘卡片的情况下执行发送卡片以及提示消息
                            if (!hasFinishSendOneTime && arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.SendOneTime) {
                                hasFinishSendOneTime = true
                                // 发送楼盘卡片
                                sendHousecardDirectly(arguments!!.getSerializable(CommonParams.HOUSEITEM) as HouseItem)
                            }
                            // 数据同步完成之后在需要发送VR卡片的情况下执行发送卡片以及提示消息
                            else if (!hasFinishSendOneTime && arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.VR) {
                                hasFinishSendOneTime = true
                                // 发送VR卡片
                                sendVRCard(arguments!!.getSerializable(CommonParams.VRITEM) as VRItem)
                            }
                            // 消息同步完成后重新获取会话列表数据
                            vm!!.queryMessageLists(null)
                        }
                        // 收到自定义的通知，这里是"正在输入"提示
                        if (it.type == ObserveResponseType.CustomNotification) {
                            // 判断属于当前会话中的用户
                            if ((it.data as CustomNotification).sessionId == arguments!!.getString(CommonParams.ACCOUNT)) {
                                val content = (it.data as CustomNotification).content
                                try {
                                    val type = JSONObject(content).getString(CommonParams.TYPE)
                                    if (type == CommonParams.COMMAND_INPUT) {
                                        conversationListener?.titleChange(false)
                                        titleChangeHandler.postDelayed(titleChangeRunnable, 4000)
                                    }
                                } catch (e: Exception) {

                                }
                            }
                        }
                        // 收到Emoji
                        if (it.type == ObserveResponseType.Emoji) {
                            val currentPosition = editTextMessage.selectionStart
                            editTextMessage.text.insert(currentPosition, it.data as SpannableString)
                        }
                        // 收到Sticker
                        if (it.type == ObserveResponseType.Sticker) {
                            sendSticker(it.data as StickerItem)
                        }
                    }
                    .subscribe())

            // 获取会话列表数据
            vm!!.queryMessageLists(null)
        }
    }

    override fun onResume() {
        super.onResume()
        // 消息提醒场景设置，设置为当前正在聊天的对象，当前正在聊天的对象没有通知显示，其余有
        MessageManager.setChattingAccount(arguments!!.getString(CommonParams.ACCOUNT),
                if (arguments!!.getBoolean(CommonParams.ISGROUP)) SessionTypeEnum.Team else SessionTypeEnum.P2P)

        // 判断是否登录，没有登录自动执行登录
        vm!!.signIn()
    }

    override fun onPause() {
        super.onPause()

        // 需要通知显示
        MessageManager.enableMsgNotification(true)

        // 语音处理
        layout_record.onPause()

        // 停止语音播放
        MessageAudioControl.getInstance().stopAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable.dispose()

        // 语音处理
        layout_record.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            sendLocation(data!!.getParcelableExtra("LatLng"), data.getStringExtra("address"))
        }
    }

    private fun initUI() {
        // ***********************************  Emoji配置  ***********************************
        val vpFragments = ArrayList<Fragment>()
        vpFragments.add(EmojiFragment())
        val count = StickerUtils.getCategories().size
        for (i in 0 until count) {
            vpFragments.add(StickerFragment.getInstance(StickerUtils.getCategories()[i]))
        }
        val vpAdapter = VpAdapter(childFragmentManager, vpFragments)
        vp_panel_content.adapter = vpAdapter
        // ***********************************  Emoji配置  ***********************************

        // ***********************************  更多菜单配置 ***************************************
        val width = ScreenUtils.getScreenWidth()/4
        val params = LinearLayout.LayoutParams(width, width)
        val cards = arguments!!.getSerializable(CommonParams.CARD) as Array<ConversationCard>
        for (i in 0 until cards.size) {
            val view = LayoutInflater.from(activity).inflate(R.layout.view_grid_panel_content_item, null, false)
            val itemIv = view.findViewById<ImageView>(R.id.iv_view_grid_panel_content_item)
            when (cards[i]) {
                // 选择相册
                ConversationCard.ALUMNI -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_image)
                    view.setOnClickListener { conversationListener?.pickPhoto() }
                }
                ConversationCard.CAMERA -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_camera)
                    view.setOnClickListener { conversationListener?.takePhoto() }
                }
                ConversationCard.HOUSE -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_house)
                    view.setOnClickListener { conversationListener?.choiceHouse() }
                }
                ConversationCard.LOCATION -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_map)
                    view.setOnClickListener { startActivityForResult(Intent(activity, MapActivity::class.java), 2000) }
                }
                ConversationCard.EVALUATE -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_evaluate)
                    view.setOnClickListener { conversationListener?.evaluate() }
                }
                ConversationCard.TIPOFFS -> {
                    itemIv.setImageResource(R.mipmap.ic_conversation_tipoffs)
                    view.setOnClickListener { conversationListener?.tipOffs() }
                }
            }
            grid_panel_content.addView(view, params)
        }
        // ***********************************  更多菜单配置 ***************************************

        // ***********************************  JKeyboardPanelSwitch配置  ***********************************
        layout_record.setIAudioRecordCallback { audioFile, audioLength, _ ->
            // 发送语音
            sendAudio(audioFile, audioLength)
        }
        KeyboardUtil.attach(context as Activity, kp_panel_root) { isShowing ->
            if (isShowing) {
                rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }
        KPSwitchConflictUtil.attach(kp_panel_root, editTextMessage, KPSwitchConflictUtil.SwitchClickListener { switchToPanel ->
            // 点击切换功能时恢复到文本框显示状态
            audioRecord.visibility = View.GONE
            editTextMessage.visibility = View.VISIBLE
            buttonTextMessage.visibility = View.GONE
            buttonAudioMessage.visibility = View.VISIBLE

            if (switchToPanel) {
                editTextMessage.clearFocus()
                rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
            } else {
                editTextMessage.requestFocus()
            }
        }, KPSwitchConflictUtil.SubPanelAndTrigger(layout_emojichoice, emoji_button),
                KPSwitchConflictUtil.SubPanelAndTrigger(layout_content, buttonMoreFuntionInText))
        editTextMessage.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                KPSwitchConflictUtil.showKeyboard(kp_panel_root, editTextMessage)
            }
            false
        }
        kp_panel_root.post {
            val layoutParams = kp_panel_root.layoutParams as LinearLayout.LayoutParams
            // 如果卡片类型小于4种，就按照固定高度来处理，反之则使用键盘高度
            layoutParams.height = if ((arguments!!.getSerializable(CommonParams.CARD) as Array<ConversationCard>).size > 4) {
                KeyboardUtil.getKeyboardHeight(context)
            } else {
                SizeUtils.dp2px(126f)
            }
            kp_panel_root.layoutParams = layoutParams
        }
        // ***********************************  JKeyboardPanelSwitch配置  ***********************************

        // 触摸到RecyclerView之后自动收起面板
        rv_conversation.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                isExecuteCollapse = false
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                if (!isExecuteCollapse) {
                    isExecuteCollapse = true
                    KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root)
                }
            }
            false
        }
        rv_conversation.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onScrolled()

                // 上拉加载更多
                val canScrollDown = rv_conversation.canScrollVertically(-1)
                if (!canScrollDown && !isPullMessageHistory) {
                    isPullMessageHistory = true
                    vm!!.pullMessageHistory(false)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrollStateChanged(newState)
            }
        })
        audioRecord.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 停止语音播放
                MessageAudioControl.getInstance().stopAudio()
            }
            layout_record.onPressToSpeakBtnTouch(v, event)
            if (event.action == MotionEvent.ACTION_UP ||
                    event.action == MotionEvent.ACTION_CANCEL) {
                audioRecord.setBackgroundResource(R.drawable.shape_message_receiver)
                audioRecord.textColor = Color.parseColor("#888888")
            }
            else {
                audioRecord.setBackgroundResource(R.drawable.shape_message_audio)
                audioRecord.textColor = Color.WHITE
            }
            return@setOnTouchListener false
        }
        // 点击键盘按键
        buttonTextMessage.setOnClickListener {
            audioRecord.visibility = View.GONE
            editTextMessage.visibility = View.VISIBLE
            editTextMessage.requestFocus()
            buttonTextMessage.visibility = View.GONE
            buttonAudioMessage.visibility = View.VISIBLE

            KPSwitchConflictUtil.showKeyboard(kp_panel_root, editTextMessage)
        }
        // 点击显示录音按钮
        buttonAudioMessage.setOnClickListener {
            KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root)

            audioRecord.visibility = View.VISIBLE
            editTextMessage.visibility = View.GONE
            buttonTextMessage.visibility = View.VISIBLE
            buttonAudioMessage.visibility = View.GONE
        }
        editTextMessage.setOnFocusChangeListener { _, _ ->
            editTextMessage.hint = ""
            checkSendButtonEnable(editTextMessage)
        }
        editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                checkSendButtonEnable(editTextMessage)
                // 发送"正在输入"提示
                vm!!.sendTypingCommand()
            }
        })
        // 显示录音按钮
        buttonTextMessage.visibility = View.GONE
        buttonAudioMessage.visibility = View.VISIBLE
        // 显示更多按钮
        checkSendButtonEnable(editTextMessage)
    }

    /**
     * 显示发送或更多
     *
     * @param editText
     */
    private fun checkSendButtonEnable(editText: EditText) {
        val textMessage = editText.text.toString()
        if (!TextUtils.isEmpty(textMessage) && editText.hasFocus()) {
            buttonMoreFuntionInText.visibility = View.GONE
            buttonSendMessage.visibility = View.VISIBLE
        } else {
            buttonSendMessage.visibility = View.GONE
            buttonMoreFuntionInText.visibility = View.VISIBLE
        }
    }

    override fun click(view: View) {
        super.click(view)
        when(view.id) {
            R.id.buttonSendMessage -> {
                // 发送文本
                sendText()
            }
        }
    }

    class VpAdapter(fragmentManager: FragmentManager, private val fragments: ArrayList<Fragment>) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size
    }

    /**
     * 判断是否需要发送完消息之后补充发送用户信息卡片
     */
    private fun isSendUserInfoAfterSend(imMessage: IMMessage) {
        if (!hasFinishSendUserInfoAfterSend && arguments!!.getSerializable(CommonParams.TYPE) == CONVERSATIONTYPE.SendUserInfoAfterSend) {
            hasFinishSendUserInfoAfterSend = true
            vm!!.isSendUserInfoAfterSend(imMessage, true, arguments!!.getString(CommonParams.USERINFO))
        }
        else {
            vm!!.isSendUserInfoAfterSend(imMessage, false, "")
        }
    }

    /**
     * 发送文本
     */
    private fun sendText() {
        if (TextUtils.isEmpty(editTextMessage.text.toString())) {
            return
        }
        val imMessage = vm!!.prepareText(editTextMessage.text.toString())
        if (imMessage != null) {
            // 判断是否需要发送完消息之后补充发送用户信息卡片
            isSendUserInfoAfterSend(imMessage)
            // 重置文本框
            editTextMessage.setText("")
            smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
        }
    }

    /**
     * 选择完图片后回传
     */
    fun sendImageFile(file: File) {
        Handler().postDelayed({
            val imMessage = vm!!.prepareImageFile(file)
            if (imMessage != null) {
                // 判断是否需要发送完消息之后补充发送用户信息卡片
                isSendUserInfoAfterSend(imMessage)
                smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }, 500)
    }

    /**
     * 发送语音
     */
    private fun sendAudio(file: File, duration: Long) {
        Handler().postDelayed({
            val imMessage = vm!!.prepareAudio(file, duration)
            if (imMessage != null) {
                // 判断是否需要发送完消息之后补充发送用户信息卡片
                isSendUserInfoAfterSend(imMessage)
                smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }, 500)
    }

    /**
     * 发送地理位置
     */
    private fun sendLocation(latLng: LatLng, address: String) {
        Handler().postDelayed({
            val imMessage = vm!!.prepareLocation(latLng, address)
            if (imMessage != null) {
                // 判断是否需要发送完消息之后补充发送用户信息卡片
                isSendUserInfoAfterSend(imMessage)
                smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }, 500)
    }

    /**
     * 发送贴图消息
     */
    private fun sendSticker(stickerItem: StickerItem) {
        Handler().postDelayed({
            val imMessage = vm!!.prepareSticker(stickerItem)
            if (imMessage != null) {
                // 判断是否需要发送完消息之后补充发送用户信息卡片
                isSendUserInfoAfterSend(imMessage)
                smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }, 500)
    }

    /**
     * 发送楼盘卡片消息
     */
    fun sendHousecard(houseItem: HouseItem) {
        Handler().postDelayed({
            sendHousecardDirectly(houseItem)
        }, 500)
    }

    /**
     * 发送楼盘卡片消息
     */
    private fun sendHousecardDirectly(houseItem: HouseItem) {
        val imMessage = vm!!.prepareHouseCard(houseItem)
        if (imMessage != null) {
            vm!!.refreshSendIMMessage(imMessage)
            smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
        }
    }

    /**
     * 发送楼盘卡片消息
     */
    private fun sendVRCard(vrItem: VRItem) {
        val imMessage = vm!!.prepareVRCard(vrItem)
        if (imMessage != null) {
            // 更新最新VR卡片UUID
            ConversationFragment.currentVRUUID = imMessage.uuid

            vm!!.refreshSendIMMessage(imMessage)
            smoothMoveToPosition(rv_conversation.adapter.itemCount - 1)
        }
    }

    /**
     * 删除消息
     */
    fun deleteIMMessage(imMessage: IMMessage) {
        vm!!.deleteIMMessage(imMessage)
    }

    /**
     * 消息撤回
     */
    fun sendRevokeIMMessage(imMessage: IMMessage) {
        vm!!.sendRevokeIMMessage(imMessage)
    }

    /**
     * 复制文字消息
     */
    fun copyIMMessage(imMessage: IMMessage) {
        vm!!.copyIMMessage(imMessage)
    }

    /**
     * 返回键处理
     */
    fun canBackPressed(): Boolean {
        if (kp_panel_root.visibility == View.VISIBLE) {
            KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root)
            return false
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hasFinishSendOneTime", hasFinishSendOneTime)
        outState.putBoolean("hasFinishSendUserInfoAfterSend", hasFinishSendUserInfoAfterSend)
    }

    private var mIndex = 0
    private var move = false

    private fun smoothMoveToPosition(n: Int) {
        mIndex = n
        val firstItem = (rv_conversation.layoutManager as WrapContentLinearLayoutManager).findFirstVisibleItemPosition()
        val lastItem = (rv_conversation.layoutManager as WrapContentLinearLayoutManager).findLastVisibleItemPosition()
        when {
            n <= firstItem -> rv_conversation.smoothScrollToPosition(n)
            n <= lastItem -> {
                val top = rv_conversation.getChildAt(n - firstItem).top
                rv_conversation.smoothScrollBy(0, top)
            }
            else -> {
                rv_conversation.smoothScrollToPosition(n)
                move = true
            }
        }
    }

    private fun moveToPosition(n: Int) {
        mIndex = n
        val firstItem = (rv_conversation.layoutManager as WrapContentLinearLayoutManager).findFirstVisibleItemPosition()
        val lastItem = (rv_conversation.layoutManager as WrapContentLinearLayoutManager).findLastVisibleItemPosition()
        when {
            n <= firstItem -> rv_conversation.scrollToPosition(n)
            n <= lastItem -> {
                val top = rv_conversation.getChildAt(n - firstItem).top
                rv_conversation.scrollBy(0, top)
            }
            else -> {
                rv_conversation.scrollToPosition(n)
                move = true
            }
        }
    }

    private fun onScrollStateChanged(newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onScrolled()
        }
    }

    private fun onScrolled() {
        if (move) {
            move = false
            val n = mIndex - (rv_conversation.layoutManager as WrapContentLinearLayoutManager).findFirstVisibleItemPosition()
            if ( 0 <= n && n < rv_conversation.childCount) {
                val top = rv_conversation.getChildAt(n).top
                rv_conversation.scrollBy(0, top)
            }
        }
    }


    /**
     * 判断当前显示的是不是最后一条
     */
    private fun isLastMessageVisible(): Boolean {
        val layoutManager = rv_conversation.layoutManager as WrapContentLinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        return lastVisiblePosition >= vm?.adapter?.itemCount!! - 1
    }
}