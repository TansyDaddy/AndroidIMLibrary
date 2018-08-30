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
import com.renyu.nimapp.params.InitParams
import com.renyu.nimapp.ui.view.QPopuWindow
import com.renyu.nimlibrary.bean.HouseItem
import com.renyu.nimlibrary.bean.VRItem
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import java.io.File

class ConversationActivity : BaseActivity(), ConversationFragment.ConversationListener {
    // 进入会话详情的类型
    enum class CONVERSATIONTYPE {
        // VR带看
        VR,
        // UNSPECIFIED
        UNSPECIFIED
    }

    private var conversationFragment: ConversationFragment? = null

    private var rawX: Int = 0

    companion object {
        /**
         * 基本场景
         */
        @JvmStatic
        fun gotoConversationActivity(context: Context, account: String) {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            intent.putExtra("type", CONVERSATIONTYPE.UNSPECIFIED)
            context.startActivity(intent)
        }

        /**
         * 发送卡片和文字
         */
        fun gotoConversationActivityWithTextAndCard(context: Context, account: String, text: String) {
            // 发送文字
            MessageManager.sendTextMessage(account, text)
            // 发送楼盘卡片
            MessageManager.sendHouseCardMessage(HouseItem(
                    "https://realsee.com/lianjia/Zo2183oENp9wKvyQ/N2j4qeoMWnP4ZH9cxhGHB0lB876Kv0Qg/",
                    "明华清园 3室2厅 690万",
                    "http://ke-image.ljcdn.com/320100-inspection/test-856ed6fe-b82d-4c97-a536-642050cd35d7.png.280x210.jpg",
                    "1"), "楼盘卡片")

            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            intent.putExtra("type", CONVERSATIONTYPE.UNSPECIFIED)
            context.startActivity(intent)
        }

        /**
         * 发送卡片
         */
        fun gotoConversationActivityWithCard(context: Context, account: String) {
            // 发送楼盘卡片
            MessageManager.sendHouseCardMessage(HouseItem(
                    "https://realsee.com/lianjia/Zo2183oENp9wKvyQ/N2j4qeoMWnP4ZH9cxhGHB0lB876Kv0Qg/",
                    "明华清园 3室2厅 690万",
                    "http://ke-image.ljcdn.com/320100-inspection/test-856ed6fe-b82d-4c97-a536-642050cd35d7.png.280x210.jpg",
                    "1"), "楼盘卡片")

            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            intent.putExtra("type", CONVERSATIONTYPE.UNSPECIFIED)
            context.startActivity(intent)
        }

        /**
         * 发送文字
         */
        fun gotoConversationActivityWithText(context: Context, account: String, text: String) {
            // 发送文字
            MessageManager.sendTextMessage(account, text)

            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            intent.putExtra("type", CONVERSATIONTYPE.UNSPECIFIED)
            context.startActivity(intent)
        }

        /**
         * 进入VR带看流程
         */
        @JvmStatic
        fun gotoConversationActivityWithVR(context: Context, account: String) {
            // 发送VR卡片
            val uuid = MessageManager.sendVRCardMessage(VRItem(
                    "https://realsee.com/lianjia/Zo2183oENp9wKvyQ/N2j4qeoMWnP4ZH9cxhGHB0lB876Kv0Qg/",
                    "明华清园 3室2厅 690万",
                    "http://ke-image.ljcdn.com/320100-inspection/test-856ed6fe-b82d-4c97-a536-642050cd35d7.png.280x210.jpg"))

            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            intent.putExtra("type", CONVERSATIONTYPE.VR)
            // 发送当前VR卡片的uuid作为可判断点击
            intent.putExtra("uuid", uuid)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            InitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        loadFragment(intent)
    }

    /**
     * 加载Fragment
     */
    private fun loadFragment(intent: Intent) {
        val cards = if (InitParams.isAgent) {
            arrayOf(ConversationFragment.ConversationCard.ALUMNI,
                    ConversationFragment.ConversationCard.CAMERA,
                    ConversationFragment.ConversationCard.HOUSE,
                    ConversationFragment.ConversationCard.LOCATION,
                    ConversationFragment.ConversationCard.EVALUATE)
        } else {
            arrayOf(ConversationFragment.ConversationCard.ALUMNI,
                    ConversationFragment.ConversationCard.CAMERA,
                    ConversationFragment.ConversationCard.HOUSE,
                    ConversationFragment.ConversationCard.LOCATION,
                    ConversationFragment.ConversationCard.TIPOFFS)
        }
        // 区分是否已经发送过VR卡片的场景
        conversationFragment = if (intent.getSerializableExtra("type") == CONVERSATIONTYPE.VR) {
            ConversationFragment.getInstanceWithVRCard(intent.getStringExtra("account"),
                    intent.getStringExtra("uuid"),
                    intent.getBooleanExtra("isGroup", false),
                    cards)
        } else {
            ConversationFragment.getInstance(intent.getStringExtra("account"),
                    intent.getBooleanExtra("isGroup", false),
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
     * 发送房源
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

        QPopuWindow.getInstance(view.context).builder
                .bindView(view, choicePosition)
                .setPopupItemList(if (imMessage.msgType == MsgTypeEnum.text) arrayOf("复制", "删除", "撤回") else arrayOf("删除", "撤回"))
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