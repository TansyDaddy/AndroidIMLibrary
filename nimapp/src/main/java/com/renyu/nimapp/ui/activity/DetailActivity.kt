package com.renyu.nimapp.ui.activity

import android.os.Bundle
import com.renyu.nimapp.R
import com.renyu.nimapp.params.NimInitParams
import com.renyu.nimlibrary.bean.VRItem

class DetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        if (!NimInitParams.isAgent) {
            // 发送VR看房看片
            ConversationActivity.gotoConversationActivityWithVR(this,
                    "r17171709",
                    "r17171709正在为您服务",
                    VRItem("210435955",
                            "上海路 金银街北阴阳营 西桥花苑 东边户3房 采光好",
                            "http://img32.house365.com//M03//39//E0//rBEBYFru5aOARtRwAAIVgGG_UwU386_760x600_c.jpg",
                            "3室1厅1卫",
                            "79.54",
                            "湖南路",
                            "西桥花苑",
                            ""))
            // 发送提示消息
//            ConversationActivity.gotoConversationActivityWithTip(this, "r17171709", "r17171709正在为您服务")
            // 发送楼盘卡片
//            ConversationActivity.gotoConversationActivityWithCard(this,
//                    "r17171709",
//                    "r17171709正在为您服务",
//                    HouseItem("210435955",
//                            "上海路 金银街北阴阳营 西桥花苑 东边户3房 采光好",
//                            "http://img32.house365.com//M03//39//E0//rBEBYFru5aOARtRwAAIVgGG_UwU386_760x600_c.jpg",
//                            "3室1厅1卫",
//                            "79.54",
//                            "湖南路",
//                            "西桥花苑",
//                            ""))
            // 发送用户信息
//            ConversationActivity.gotoConversationActivityWithUserInfo(this, "r17171709", "r17171709正在为您服务", "来自{淘房APP}，正在浏览{项目名称}")
        }
        finish()
    }
}