package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.ViewModel
import com.blankj.utilcode.util.SPUtils
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.manager.FriendManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.adapter.ContactAdapter
import com.renyu.nimlibrary.util.RxBus

class ContactViewModel : ViewModel() {

    private val userInfos: ArrayList<NimUserInfo> by lazy {
        ArrayList<NimUserInfo>()
    }

    val adapter: ContactAdapter by lazy {
        ContactAdapter(userInfos)
    }

    /**
     * 获取全部好友
     */
    fun getUserInfoOfMyFriends() {
        val accounts: ArrayList<String> = FriendManager.getFriendAccounts() as ArrayList<String>
        val blacks = FriendManager.getBlackList()
        // 去除黑名单用户
        accounts.removeAll(blacks)
        // 去除自己
        accounts.remove(SPUtils.getInstance().getString(CommonParams.SP_UNAME))
        userInfos.clear()
        userInfos.addAll(UserManager.getUserInfoList(accounts))
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新好友列表
     */
    fun updateFriends(notify: FriendChangedNotify) {
        // 返回增加或者发生变更的好友关系
        val addedOrUpdatedFriends = notify.addedOrUpdatedFriends
        // 返回被删除的的好友关系
        val deletedFriendAccounts = notify.deletedFriends

        // 删除不是好友的账号
        val deleteFriends = ArrayList<NimUserInfo>()
        userInfos.forEach {
            if (deletedFriendAccounts.contains(it.account)) {
                deleteFriends.add(it)
            }
        }
        userInfos.removeAll(deleteFriends)

        // 去除需要更新的老账号
        val sameFriends = ArrayList<NimUserInfo>()
        addedOrUpdatedFriends.forEach {
            userInfos.forEach find@ { userInfo ->
                if (userInfo.account == it.account) {
                    sameFriends.add(userInfo)
                    return@find
                }
            }
        }
        userInfos.removeAll(sameFriends)
        // 添加新好友
        addedOrUpdatedFriends.forEach {
            val userInfo = UserManager.getUserInfo(it.account)
            // 本地缓存中有好友信息，则直接获取，否则从云端获取
            if (userInfo != null) {
                userInfos.add(userInfo)
            }
            else {
                addNewFriendInfoByNetWork(it.account)
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新好友列表中的黑名单账户
     */
    fun updateBlackList(notify: BlackListChangedNotify) {
        // 被加入到黑名单的用户账号
        val addedAccounts = notify.addedAccounts
        // 移出黑名单的用户账号
        val removedAccounts = notify.removedAccounts

        // 删除添加到黑名单的账号
        val deleteFriends = ArrayList<NimUserInfo>()
        userInfos.filter {
            addedAccounts.contains(it.account)
        }.forEach {
            deleteFriends.add(it)
        }
        userInfos.removeAll(deleteFriends)

        // 添加移出黑名单的账号
        removedAccounts.forEach {
            val userInfo = UserManager.getUserInfo(it)
            // 本地缓存中有好友信息，则直接获取，否则从云端获取
            if (userInfo != null) {
                userInfos.add(userInfo)
            }
            else {
                addNewFriendInfoByNetWork(it)
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 获取云端好友信息
     */
    private fun addNewFriendInfoByNetWork(account: String) {
        val arrayList = ArrayList<String>()
        arrayList.add(account)
        UserManager.fetchUserInfo(arrayList, object : RequestCallback<List<NimUserInfo>> {
            override fun onSuccess(param: List<NimUserInfo>?) {
                if (param?.size != 0) {
                    RxBus.getDefault().post(ObserveResponse(param, ObserveResponseType.FetchUserInfoByContact))
                    if (param != null) {
                        userInfos.addAll(param)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable?) {

            }
        })
    }
}