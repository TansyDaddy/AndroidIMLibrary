package com.renyu.nimapp.ui.activity

import android.os.Bundle
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimlibrary.ui.fragment.ContactFragment

class ContactActivity : BaseActivity(), ContactFragment.ContactListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            NimInitParams.isRestore = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, ContactFragment(), "contactFragment")
                .commitAllowingStateLoss()
    }

    /**
     * 打开个人详情
     */
    override fun gotoUserInfo(account: String) {

    }

    /**
     * 点击联系人列表
     */
    override fun clickContact(nimUserInfo: NimUserInfo) {

    }
}