package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import com.renyu.nimapp.R
import com.renyu.nimapp.params.InitParams
import com.renyu.nimlibrary.bean.VRItem
import com.renyu.nimlibrary.extension.VRAttachment
import com.renyu.nimlibrary.manager.MessageManager

class DetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 客户从VR看房模块进入IM，需要发送卡片
        if (!InitParams.isAgent) {
            // 发送VR信息
            val uuid = sendVR(VRItem(
                    "https://realsee.com/lianjia/Zo2183oENp9wKvyQ/N2j4qeoMWnP4ZH9cxhGHB0lB876Kv0Qg/",
                    "明华清园 3室2厅 690万",
                    "http://ke-image.ljcdn.com/320100-inspection/test-856ed6fe-b82d-4c97-a536-642050cd35d7.png.280x210.jpg"))
            val intent = Intent(this, ConversationActivity::class.java)
            intent.putExtra("account", "r17171709")
            intent.putExtra("isGroup", false)
            // 发送当前VR卡片的uuid作为可判断点击
            intent.putExtra("uuid", uuid)
            startActivity(intent)
        }
    }

    /**
     * 发送VR消息
     */
    private fun sendVR(vrItem: VRItem): String {
        val attachment = VRAttachment(vrItem.vrJson)
        val imMessage = MessageManager.sendCustomMessage("r17171709", "VR", attachment)
        return imMessage.uuid
    }
}