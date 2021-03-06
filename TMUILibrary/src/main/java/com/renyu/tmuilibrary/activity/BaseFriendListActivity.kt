package com.renyu.tmuilibrary.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.Utils
import com.focustech.dbhelper.PlainTextDBHelper
import com.focustech.message.model.BroadcastBean
import com.focustech.message.model.FriendGroupRsp
import com.focustech.message.model.FriendInfoRsp
import com.focustech.message.model.UserInfoRsp
import com.renyu.tmbaseuilibrary.base.BaseIMActivity
import com.renyu.tmbaseuilibrary.params.CommonParams
import com.renyu.tmbaseuilibrary.service.MTService
import com.renyu.tmuilibrary.fragment.FriendListFragment
import java.util.ArrayList

abstract class BaseFriendListActivity : BaseIMActivity() {

    @JvmField var friendListFragment: FriendListFragment? = null

    override fun initParams() {
        // 存在回收之后再次回收，造成下线标志位出错
        if (checkNullInfo()) {
            return
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == actionName) {
                    val bean = intent!!.getSerializableExtra("broadcast") as BroadcastBean
                    // 获取好友关系组数据
                    if (bean.command == BroadcastBean.MTCommand.FriendGroupsRsp) {
                        // 清除所有好友组关系数据
                        PlainTextDBHelper.getInstance(Utils.getApp()).clearAllFriendList()
                        // 增加所有好友组关系数据
                        PlainTextDBHelper.getInstance(Utils.getApp()).insertFriendList((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable!! as ArrayList<FriendGroupRsp>)
                        // 通知好友列表刷新
                        friendListFragment?.refreshFriendList()
                        // 获取完成组信息之后，就获取全部好友信息
                        MTService.reqFriendInfo(Utils.getApp())

                        friendListFragment?.endRefresh()
                    }
                    if (bean.command == BroadcastBean.MTCommand.FriendInfoRsp) {
                        val temp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as FriendInfoRsp
                        // 插入好友数据
                        PlainTextDBHelper.getInstance(Utils.getApp()).insertFriendList(temp.friend)
                        // 刷新好友列表
                        friendListFragment?.refreshFriendInfo(temp.friend)
                    }
                    // 删除好友
                    if (bean.command == BroadcastBean.MTCommand.DeleteFriendRsp) {
                        val userId = (bean.serializable as UserInfoRsp).userId
                        // 刷新好友列表
                        friendListFragment?.deleteFriendsRelation(userId)
                    }
                    // 添加与被添加好友
                    if (bean.command == BroadcastBean.MTCommand.RefreshFriendList) {
                        MTService.reqFriendGroups(Utils.getApp())
                    }
                    // 被踢下线
                    if (bean.command == BroadcastBean.MTCommand.Kickout) {
                        CommonParams.isKickout = true
                        if (!isPause) {
                            kickout()
                        }
                    }
                }
            }
        }
        openCurrentReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCurrentReceiver()
    }
}