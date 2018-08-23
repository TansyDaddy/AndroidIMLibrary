package com.renyu.nimlibrary.binding

import android.view.View
import com.netease.nimlib.sdk.msg.model.IMMessage

interface EventImpl {
    fun click(view: View) {}

    // 删除联系人
    fun deleteRecentContact(view: View, contactId: String) {}

    // 跳转会话详情
    fun gotoConversationActivity(view: View, account: String) {}

    // 重新发送消息
    fun resendIMMessage(view: View, uuid: String) {}

    // 长按消息列表中的消息
    fun onLongClick(view: View, imMessage: IMMessage): Boolean {
        return true
    }

    // 客户前往VR去电页面
    fun gotoVrOutgoingCall(view: View, imMessage: IMMessage) {}

    // 经纪人前往VR来电页面
    fun gotoVrInComingCall(view: View, imMessage: IMMessage) {}

    // 前往地图预览页面
    fun gotoMapPreview(view: View, imMessage: IMMessage) {}
}