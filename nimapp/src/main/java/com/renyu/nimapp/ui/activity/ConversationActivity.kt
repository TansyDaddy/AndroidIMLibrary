package com.renyu.nimapp.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.BarUtils
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimapp.ui.view.QPopuWindow
import com.renyu.nimlibrary.bean.HouseItem
import com.renyu.nimlibrary.bean.VRItem
import com.renyu.nimlibrary.extension.HouseAttachment
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import java.io.File

class ConversationActivity : BaseActivity(), ConversationFragment.ConversationListener {


    private var conversationFragment: ConversationFragment? = null

    private var rawX: Int = 0

    companion object {
        /**
         * 基本场景
         */
        @JvmStatic
        fun gotoConversationActivity(context: Context, account: String) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CommonParams.ACCOUNT, account)
            intent.putExtra(CommonParams.ISGROUP, false)
            intent.putExtra(CommonParams.TYPE, ConversationFragment.CONVERSATIONTYPE.UNSPECIFIED)
            context.startActivity(intent)
        }

        /**
         * 发送带提示的消息
         */
        @JvmStatic
        fun gotoConversationActivityWithTip(context: Context, account: String, tip: String) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CommonParams.ACCOUNT, account)
            intent.putExtra(CommonParams.ISGROUP, false)
            intent.putExtra(CommonParams.TIP, tip)
            intent.putExtra(CommonParams.TYPE, ConversationFragment.CONVERSATIONTYPE.TIP)
            context.startActivity(intent)
        }

        /**
         * 用户主动发送一条信息后触发发送用户信息
         */
        @JvmStatic
        fun gotoConversationActivityWithUserInfo(context: Context, account: String, tip: String, userInfo: String) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CommonParams.ACCOUNT, account)
            intent.putExtra(CommonParams.ISGROUP, false)
            intent.putExtra(CommonParams.TIP, tip)
            intent.putExtra(CommonParams.TYPE, ConversationFragment.CONVERSATIONTYPE.SendUserInfoAfterSend)
            intent.putExtra(CommonParams.USERINFO, userInfo)
            context.startActivity(intent)
        }

        /**
         * 每一个新的聊天对象触发一次发送楼盘卡片
         */
        @JvmStatic
        fun gotoConversationActivityWithCard(context: Context, account: String, tip: String, houseItem: HouseItem) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CommonParams.ACCOUNT, account)
            intent.putExtra(CommonParams.ISGROUP, false)
            intent.putExtra(CommonParams.HOUSEITEM, houseItem)
            intent.putExtra(CommonParams.TIP, tip)
            intent.putExtra(CommonParams.TYPE, ConversationFragment.CONVERSATIONTYPE.SendOneTime)
            context.startActivity(intent)
        }

        /**
         * 进入VR带看流程
         */
        @JvmStatic
        fun gotoConversationActivityWithVR(context: Context, account: String, tip: String, vrItem: VRItem) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CommonParams.ACCOUNT, account)
            intent.putExtra(CommonParams.ISGROUP, false)
            intent.putExtra(CommonParams.TIP, tip)
            intent.putExtra(CommonParams.TYPE, ConversationFragment.CONVERSATIONTYPE.VR)
            intent.putExtra(CommonParams.VRITEM, vrItem)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            NimInitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        loadFragment(intent)
    }

    /**
     * 加载Fragment
     */
    private fun loadFragment(intent: Intent) {
        val cards = if (NimInitParams.isAgent) {
            arrayOf(ConversationFragment.ConversationCard.ALUMNI,
                    ConversationFragment.ConversationCard.CAMERA,
                    ConversationFragment.ConversationCard.HOUSE,
                    ConversationFragment.ConversationCard.LOCATION)
        } else {
            arrayOf(ConversationFragment.ConversationCard.ALUMNI,
                    ConversationFragment.ConversationCard.CAMERA,
                    ConversationFragment.ConversationCard.HOUSE,
                    ConversationFragment.ConversationCard.LOCATION,
                    ConversationFragment.ConversationCard.TIPOFFS)
        }
        // 区分是否已经发送过VR卡片的场景
        conversationFragment = when(intent.getSerializableExtra(CommonParams.TYPE)) {
            ConversationFragment.CONVERSATIONTYPE.VR -> ConversationFragment.getInstanceWithVRCard(intent.getStringExtra(CommonParams.ACCOUNT),
                    intent.getSerializableExtra(CommonParams.VRITEM) as VRItem,
                    intent.getBooleanExtra(CommonParams.ISGROUP, false),
                    cards,
                    intent.getStringExtra(CommonParams.TIP))
            ConversationFragment.CONVERSATIONTYPE.TIP -> ConversationFragment.getInstanceWithTip(intent.getStringExtra(CommonParams.ACCOUNT),
                    intent.getBooleanExtra(CommonParams.ISGROUP, false),
                    cards,
                    intent.getStringExtra(CommonParams.TIP))
            ConversationFragment.CONVERSATIONTYPE.SendUserInfoAfterSend -> ConversationFragment.getInstanceWithSendUserInfoAfterSend(intent.getStringExtra(CommonParams.ACCOUNT),
                    intent.getStringExtra(CommonParams.USERINFO),
                    intent.getBooleanExtra(CommonParams.ISGROUP, false),
                    cards,
                    intent.getStringExtra(CommonParams.TIP))
            ConversationFragment.CONVERSATIONTYPE.SendOneTime -> ConversationFragment.getInstanceWithSendOneTime(intent.getStringExtra(CommonParams.ACCOUNT),
                    intent.getSerializableExtra(CommonParams.HOUSEITEM) as HouseItem,
                    intent.getBooleanExtra(CommonParams.ISGROUP, false),
                    cards,
                    intent.getStringExtra(CommonParams.TIP))
            else -> ConversationFragment.getInstance(intent.getStringExtra(CommonParams.ACCOUNT),
                    intent.getBooleanExtra(CommonParams.ISGROUP, false),
                    cards)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_conversation, conversationFragment)
                .commitAllowingStateLoss()
    }

    /**
     * "正在输入"
     */
    override fun titleChange(reset: Boolean) {
        if (reset) {
            Log.d("NIM_APP", "恢复")
        }
        else {
            Log.d("NIM_APP", "正在输入...")
        }
    }

    /**
     * 拍照
     */
    override fun takePhoto() {
        val intent = Intent(this, TakePhotoActivity::class.java)
        intent.putExtra("key", "value")
        startActivityForResult(intent, 1000)
    }

    /**
     * 选择相册
     */
    override fun pickPhoto() {
        val intent = Intent(this, PickPhotoActivity::class.java)
        intent.putExtra("key", "value")
        startActivityForResult(intent, 1000)
    }

    /**
     * 浏览大图
     */
    override fun showBigImage(images: ArrayList<String>, index: Int) {
        val intent = Intent(this, PickPhotoActivity::class.java)
        intent.putExtra("images", images)
        intent.putExtra("index", index)
        startActivityForResult(intent, 1000)
    }

    /**
     * 选择楼盘
     */
    override fun choiceHouse() {

    }

    /**
     * 发起评价
     */
    override fun evaluate() {

    }

    /**
     * 打开个人详情
     */
    override fun gotoUserInfo(account: String) {

    }

    /**
     * 打开楼盘卡片
     */
    override fun openHouseCard(imMessage: IMMessage) {

    }

    /**
     * 举报
     */
    override fun tipOffs() {

    }

    /**
     * 长按列表
     */
    override fun longClick(view: View, imMessage: IMMessage, choicePosition: Int) {
        val location = intArrayOf(0, 0)
        view.getLocationInWindow(location)

        // 只有文本消息、图片消息、语音消息、楼盘消息才能出现功能按键
        if (imMessage.msgType == MsgTypeEnum.text ||
                imMessage.msgType == MsgTypeEnum.image ||
                imMessage.msgType == MsgTypeEnum.audio ||
                (imMessage.msgType == MsgTypeEnum.custom && imMessage.attachment is HouseAttachment)) {
            QPopuWindow.getInstance(view.context).builder
                    .bindView(view, choicePosition)
                    .setPopupItemList(
                            if (imMessage.msgType == MsgTypeEnum.text) arrayOf("复制", "删除", "撤回")
                            else if (imMessage.msgType == MsgTypeEnum.custom && imMessage.attachment is HouseAttachment) arrayOf("删除")
                            else arrayOf("删除", "撤回"))
                    .setPointers(rawX, location[1] + BarUtils.getStatusBarHeight())
                    .setOnPopuListItemClickListener { _, _, position ->
                        if (imMessage.msgType == MsgTypeEnum.text) {
                            when(position) {
                                0 -> conversationFragment?.copyIMMessage(imMessage)
                                1 -> conversationFragment?.deleteIMMessage(imMessage)
                                2 -> conversationFragment?.sendRevokeIMMessage(imMessage)
                            }
                        }
                        else {
                            when(position) {
                                0 -> conversationFragment?.deleteIMMessage(imMessage)
                                1 -> conversationFragment?.sendRevokeIMMessage(imMessage)
                            }
                        }
                    }.show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        rawX = ev!!.rawX.toInt()
        return super.dispatchTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            // 回调fragment去发送图片
            conversationFragment?.sendImageFile(File(data?.getStringExtra("fileName")))
        }
    }

    /**
     * 返回的时候先判断面板是否开启
     */
    override fun onBackPressed() {
        if (conversationFragment!!.canBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null)
            loadFragment(intent)
    }
}