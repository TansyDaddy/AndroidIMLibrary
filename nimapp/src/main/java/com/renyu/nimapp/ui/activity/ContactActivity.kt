package com.renyu.nimapp.ui.activity

import android.os.Bundle
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimlibrary.ui.fragment.ContactFragment

class ContactActivity : BaseActivity() {

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
}