package com.renyu.nimapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimapp.R
import com.renyu.nimapp.params.InitParams
import com.renyu.nimapp.ui.view.QPopuWindow
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import java.io.File

class ConversationActivity : BaseActivity(), ConversationFragment.ConversationListener {

    private var conversationFragment: ConversationFragment? = null

    private var rawX: Int = 0

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
        // 区分是否已经发送过VR卡片的场景
        conversationFragment = if (intent.getStringExtra("uuid") != null) {
            ConversationFragment.getInstanceWithVRCard(intent.getStringExtra("account"),
                    intent.getStringExtra("uuid"),
                    intent.getBooleanExtra("isGroup", false))
        } else {
            ConversationFragment.getInstance(intent.getStringExtra("account"),
                    intent.getBooleanExtra("isGroup", false))
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
     * 长按列表
     */
    override fun longClick(view: View, imMessage: IMMessage, position: Int) {
        val location = intArrayOf(0, 0)
        view.getLocationInWindow(location)

        QPopuWindow.getInstance(view.context).builder
                .bindView(view, position)
                .setPopupItemList(if (imMessage.msgType == MsgTypeEnum.text) arrayOf("复制", "删除", "撤回") else arrayOf("删除", "撤回"))
                .setPointers(rawX, location[1])
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