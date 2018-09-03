package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.ui.fragment.ChatListFragment
import kotlinx.android.synthetic.main.view_nav.*

class ChatListActivity : BaseActivity(), ChatListFragment.ChatListListener {

    private var conversationFragment: ChatListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            NimInitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)

//        tv_nav_right.text = "退出登录"
//        tv_nav_right.setOnClickListener {
//            // 退出登录
//            AuthManager.logout()
//            // 打开登录页
//            jumpToSignIn()
//        }

//        tv_nav_right.text = "好友列表"
//        tv_nav_right.setOnClickListener {
//            startActivity(Intent(this, ContactActivity::class.java))
//        }

        tv_nav_right.text = "详情"
        tv_nav_right.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()
    }

    /**
     * 会话列表点击
     */
    override fun clickRecentContact(recentContact: RecentContact) {
        ConversationActivity.gotoConversationActivity(this, recentContact.contactId)
    }

    override fun onBackPressed() {
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.addCategory(Intent.CATEGORY_HOME)
//        startActivity(intent)

        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(NimInitParams.TYPE, NimInitParams.FINISH)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}