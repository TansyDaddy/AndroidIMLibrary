package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.SPUtils
import com.netease.nimlib.sdk.StatusCode
import com.renyu.nimapp.R
import com.renyu.nimapp.params.InitParams
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.fragment.ChatListFragment
import kotlinx.android.synthetic.main.view_nav.*

class ChatListActivity : BaseActivity() {

    private var conversationFragment: ChatListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            InitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)

        tv_nav_right.text = "退出登录"
        tv_nav_right.setOnClickListener {
            // 退出登录
            AuthManager.logout()
            // 打开登录页
            jumpToSignIn()
        }

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()

        // 这里只是随便加一个登录位置，因为采用手动登录
        // 如果用户没有登录就进入该页面，说明之前已经登录成功，在这里手动登录
        if (AuthManager.getStatus() != StatusCode.LOGINED) {
            AuthManager.login(SPUtils.getInstance().getString(CommonParams.SP_UNAME),
                    SPUtils.getInstance().getString(CommonParams.SP_PWD))
        }
    }

    override fun onBackPressed() {
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.addCategory(Intent.CATEGORY_HOME)
//        startActivity(intent)

        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(InitParams.TYPE, InitParams.FINISH)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}