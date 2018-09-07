package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.friend.FriendServiceObserve
import com.netease.nimlib.sdk.friend.constant.VerifyType
import com.netease.nimlib.sdk.friend.model.AddFriendData
import com.netease.nimlib.sdk.friend.model.Friend
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.util.RxBus


object FriendManager {

    /**
     * 添加黑名单
     */
    @JvmStatic
    fun addToBlackList(account: String) {
        NIMClient.getService(FriendService::class.java)
                .addToBlackList(account)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        Log.d("NIM_APP", "${account}已被添加到黑名单")
                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 移除黑名单
     */
    @JvmStatic
    fun removeFromBlackList(account: String) {
        NIMClient.getService(FriendService::class.java)
                .removeFromBlackList(account)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        Log.d("NIM_APP", "${account}已从黑名单中移除")
                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 监听黑名单变更通知
     */
    @JvmStatic
    fun observeBlackListChangedNotify() {
        NIMClient.getService(FriendServiceObserve::class.java).observeBlackListChangedNotify({ t ->
            val addedAccounts = t?.addedAccounts
            val removedAccounts = t?.removedAccounts
            if (t != null) {
                RxBus.getDefault().post(ObserveResponse(t, ObserveResponseType.BlackListChangedNotify))
            }
        }, true)
    }

    /**
     * 获取黑名单中的用户列表
     */
    @JvmStatic
    fun getBlackList(): List<String> {
        return NIMClient.getService(FriendService::class.java).blackList
    }

    /**
     * 判断用户是否已被拉黑
     */
    @JvmStatic
    fun isInBlackList(account: String): Boolean {
        return NIMClient.getService(FriendService::class.java).isInBlackList(account)
    }

    /**
     * 直接添加好友
     */
    @JvmStatic
    fun addDirectFriend(account: String) {
        NIMClient.getService(FriendService::class.java).addFriend(AddFriendData(account, VerifyType.DIRECT_ADD, null))
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 选择添加好友
     */
    @JvmStatic
    fun addRequestFriend(account: String, msg: String) {
        NIMClient.getService(FriendService::class.java).addFriend(AddFriendData(account, VerifyType.VERIFY_REQUEST, msg))
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 同意/拒绝好友请求
     */
    @JvmStatic
    fun ackAddFriendRequest(account: String, agree: Boolean) {
        NIMClient.getService(FriendService::class.java).ackAddFriendRequest(account, agree)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 删除好友
     */
    @JvmStatic
    fun deleteFriend(account: String) {
        NIMClient.getService(FriendService::class.java).deleteFriend(account)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    /**
     * 监听好友关系变化通知
     */
    @JvmStatic
    fun observeFriendChangedNotify() {
        NIMClient.getService(FriendServiceObserve::class.java).observeFriendChangedNotify({ t ->
            if (t != null) {
                RxBus.getDefault().post(ObserveResponse(t, ObserveResponseType.FriendChangedNotify))
            }
        }, true)
    }

    /**
     * 获取我所有的好友帐号
     */
    @JvmStatic
    fun getFriendAccounts(): List<String> {
        return NIMClient.getService(FriendService::class.java).friendAccounts
    }

    /**
     * 根据用户账号获取好友关系
     */
    @JvmStatic
    fun getFriendByAccount(account: String): Friend {
        return NIMClient.getService(FriendService::class.java).getFriendByAccount(account)
    }

    /**
     * 是否为我的好友
     */
    @JvmStatic
    fun isMyFriend(account: String): Boolean {
        return NIMClient.getService(FriendService::class.java).isMyFriend(account)
    }
}