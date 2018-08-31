package com.renyu.nimapp.ui.activity

import android.os.Bundle
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams

class DetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 客户从VR看房模块进入IM，需要发送卡片
        if (!NimInitParams.isAgent) {
//            ConversationActivity.gotoConversationActivityWithCard(this, "r17171709")
            ConversationActivity.gotoConversationActivityWithVR(this, "r17171709")
        }
    }
}