package com.renyu.nimlibrary.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.msg.model.IMMessage
import java.util.*

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clazz = Class.forName("com.renyu.nimapp.params.InitParams")
        // 如果app被杀，则受到的消息不包含IMMessage信息
        val messages = intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT)
        if (messages == null) {
            // 打开启动页
            val initActivityName = clazz.getField("InitActivityName").get(clazz).toString()
            val initActivityClass = Class.forName(initActivityName)
            val initIntent = Intent(this, initActivityClass)
            startActivity(initIntent)
        }
        else {
            if (messages is ArrayList<*> && messages.size>0 && messages[0] is IMMessage) {
                // 跳转到会话详情
                val conversationActivityName = clazz.getField("ConversationActivityName").get(clazz).toString()
                val conversationActivityClass = Class.forName(conversationActivityName)
                val conversationIntent = Intent(this, conversationActivityClass)
                conversationIntent.putExtra("account", (messages[0] as IMMessage).sessionId)
                conversationIntent.putExtra("isGroup", false)
                startActivity(conversationIntent)
            }
        }
        finish()
    }
}