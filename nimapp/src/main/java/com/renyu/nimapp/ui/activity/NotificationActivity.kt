package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.nimapp.params.NimInitParams
import java.util.*

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 如果app被杀，则受到的消息不包含IMMessage信息
        val messages = intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT)
        if (messages == null) {
            // 打开启动页
            val initIntent = Intent(this, SplashActivity::class.java)
            startActivity(initIntent)
        }
        else {
            if (messages is ArrayList<*> && messages.size>0 && messages[0] is IMMessage) {
                // 如果App没有退出
                if (NimInitParams.isFirst) {
                    // 跳转到会话详情
                    val conversationIntent = Intent(this, ConversationActivity::class.java)
                    conversationIntent.putExtra("account", (messages[0] as IMMessage).sessionId)
                    conversationIntent.putExtra("isGroup", false)
                    startActivity(conversationIntent)
                }
                else {
                    // 打开启动页
                    val initIntent = Intent(this, SplashActivity::class.java)
                    startActivity(initIntent)
                }
            }
        }
        finish()
    }
}