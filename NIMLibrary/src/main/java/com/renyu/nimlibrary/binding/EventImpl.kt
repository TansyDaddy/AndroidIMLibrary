package com.renyu.nimlibrary.binding

import android.view.View
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo

interface EventImpl {
    fun click(view: View) {}

    // 删除联系人
    fun deleteRecentContact(view: View, contactId: String) {}

    // 跳转会话详情
    fun gotoConversationActivity(view: View, recentContact: RecentContact) {}

    // 重新发送消息
    fun resendIMMessage(view: View, uuid: String) {}

    // 长按消息列表中的消息
    fun onLongClick(view: View, imMessage: IMMessage): Boolean {
        return true
    }

    // C端前往VR去电页面
    fun gotoVrOutgoingCall(view: View, imMessage: IMMessage) {}

    // 前往地图预览页面
    fun gotoMapPreview(view: View, imMessage: IMMessage) {}

    // 前往个人详情
    fun gotoUserInfo(view: View, account: String) {}

    // 打开大图
    fun openBigImageViewActivity(view: View) {}

    // 打开房源卡片
    fun openHouseCard(view: View, imMessage: IMMessage) {}

    // 点击联系人列表
    fun clickContact(view: View, nimUserInfo: NimUserInfo) {}
}