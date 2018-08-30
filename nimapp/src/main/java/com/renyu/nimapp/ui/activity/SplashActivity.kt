package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.renyu.nimapp.R
import com.renyu.nimapp.params.InitParams
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.params.CommonParams

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        InitParams.isFirst = true

        if (CommonParams.isKickout) {
            CommonParams.isKickout = false
            // 重置回收标志位
            InitParams.isRestore = false
        }

        // 发生回收，若执行返回操作则执行页面关闭
        if (InitParams.isRestore) {
            InitParams.isFirst = false
            finish()
            return
        }

        val accid = AuthManager.getUserAccount().first
        val token = AuthManager.getUserAccount().second
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
        InitParams.isRestore = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getIntExtra(InitParams.TYPE, -1) == InitParams.FINISH) {
            InitParams.isFirst = false
            finish()
        }
        if (intent.getIntExtra(InitParams.TYPE, -1) == InitParams.KICKOUT) {
            CommonParams.isKickout = false
            startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
        }
        if (intent.getIntExtra(InitParams.TYPE, -1) == InitParams.SIGNINBACK) {
            InitParams.isFirst = false
            finish()
        }
        if (intent.getIntExtra(InitParams.TYPE, -1) == InitParams.MAIN) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
    }
}