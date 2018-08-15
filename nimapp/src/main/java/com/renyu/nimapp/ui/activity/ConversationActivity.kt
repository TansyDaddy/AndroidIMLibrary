package com.renyu.nimapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.netease.nimlib.sdk.avchat.constant.AVChatType
import com.renyu.nimapp.R
import com.renyu.nimapp.params.InitParams
import com.renyu.nimavchatlibrary.ui.AVChatActivity
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import org.json.JSONObject
import java.io.File

class ConversationActivity : BaseActivity(), ConversationFragment.ConversationListener {
    private var conversationFragment: ConversationFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            InitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        loadFragment(intent)

        // 测试
//        sendVR("http://g.hiphotos.baidu.com/image/pic/item/5bafa40f4bfbfbedc5597ab474f0f736aec31ffc.jpg")
        // 语音
//        val jsonObject = JSONObject()
//        jsonObject.put("account", intent.getStringExtra("account"))
//        AVChatActivity.outgoingCall(this, intent.getStringExtra("account"), jsonObject.toString(), AVChatType.AUDIO.value, AVChatActivity.FROM_INTERNAL)
    }

    /**
     * 加载Fragment
     */
    private fun loadFragment(intent: Intent) {
        conversationFragment = ConversationFragment.getInstance(intent.getStringExtra("account"),
                intent.getBooleanExtra("isGroup", false))
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