package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.push.HuaweiPush
import com.netease.nimlib.sdk.StatusCode
import com.renyu.nimapp.R
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.fragment.ChatListFragment


class ChatListActivity : AppCompatActivity() {

    private var conversationFragment: ChatListFragment? = null

    var client: HuaweiApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)

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

        client = HuaweiApiClient.Builder(this)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(object : HuaweiApiClient.ConnectionCallbacks {
                    override fun onConnected() {
                        val tokenResult = HuaweiPush.HuaweiPushApi.getToken(client)
                        tokenResult.setResultCallback {
                            //这边的结果只表明接口调用成功，是否能收到响应结果只在广播中接收，广播这块后面会有讲到
                            Log.d("NIM_APP", it.tokenRes.token)
                        }
                    }

                    override fun onConnectionSuspended(cause: Int) {

                    }
                })
                .addOnConnectionFailedListener {
                    Log.d("NIM_APP", "登录失败")
                }
                .build()
        client!!.connect(this)
    }

    private fun getToken() {

    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
}