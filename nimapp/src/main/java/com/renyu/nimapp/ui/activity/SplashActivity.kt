package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (NimInitParams.isFirst) {
            NimInitParams.isFirst = false
            finish()
            return
        }

        if (CommonParams.isKickout) {
            CommonParams.isKickout = false
            // 重置回收标志位
            NimInitParams.isRestore = false
        }

        // 发生回收，若执行返回操作则执行页面关闭
        if (NimInitParams.isRestore) {
            NimInitParams.isFirst = false
            finish()
            return
        }

        NimInitParams.isFirst = true

        val accid = UserManager.getUserAccount().first
        val token = UserManager.getUserAccount().second
        // 登录成功跳转首页
        if (!TextUtils.isEmpty(accid) && !TextUtils.isEmpty(token)) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
        // 没有用户信息则执行登录操作
        else {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 重置回收标志位
        NimInitParams.isRestore = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getIntExtra(NimInitParams.TYPE, -1) == NimInitParams.FINISH) {
            NimInitParams.isFirst = false
            finish()
        }
        if (intent.getIntExtra(NimInitParams.TYPE, -1) == NimInitParams.KICKOUT) {
            CommonParams.isKickout = false
            startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
        }
        if (intent.getIntExtra(NimInitParams.TYPE, -1) == NimInitParams.SIGNINBACK) {
            NimInitParams.isFirst = false
            finish()
        }
        if (intent.getIntExtra(NimInitParams.TYPE, -1) == NimInitParams.MAIN) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
    }
}