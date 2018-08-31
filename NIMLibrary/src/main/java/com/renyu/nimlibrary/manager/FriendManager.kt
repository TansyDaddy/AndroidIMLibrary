package com.renyu.nimlibrary.manager

import android.util.Log
import android.widget.Toast
import com.blankj.utilcode.util.Utils
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
    fun getBlackList(): List<String> {
        return NIMClient.getService(FriendService::class.java).blackList
    }

    /**
     * 判断用户是否已被拉黑
     */
    fun isInBlackList(account: String): Boolean {
        return NIMClient.getService(FriendService::class.java).isInBlackList(account)
    }

    /**
     * 直接添加好友
     */
    fun addDirectFriend(account: String) {
        NIMClient.getService(FriendService::class.java).addFriend(AddFriendData(account, VerifyType.DIRECT_ADD, null))
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        Toast.makeText(Utils.getApp(), "添加好友成功", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailed(code: Int) {
                        Toast.makeText(Utils.getApp(), "添加好友失败，错误编码$code", Toast.LENGTH_SHORT).show()
                    }

                    override fun onException(exception: Throwable?) {
                        Toast.makeText(Utils.getApp(), "添加好友失败", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    /**
     * 选择添加好友
     */
    fun addRequestFriend(account: String, msg: String) {
        NIMClient.getService(FriendService::class.java).addFriend(AddFriendData(account, VerifyType.VERIFY_REQUEST, msg))
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        Toast.makeText(Utils.getApp(), "添加好友请求发送成功", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailed(code: Int) {
                        Toast.makeText(Utils.getApp(), "添加好友请求发送失败，错误编码$code", Toast.LENGTH_SHORT).show()
                    }

                    override fun onException(exception: Throwable?) {
                        Toast.makeText(Utils.getApp(), "添加好友请求发送失败", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    /**
     * 同意/拒绝好友请求
     */
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
    fun getFriendAccounts(): List<String> {
        return NIMClient.getService(FriendService::class.java).friendAccounts
    }

    /**
     * 根据用户账号获取好友关系
     */
    fun getFriendByAccount(account: String): Friend {
        return NIMClient.getService(FriendService::class.java).getFriendByAccount(account)
    }

    /**
     * 是否为我的好友
     */
    fun isMyFriend(account: String): Boolean {
        return NIMClient.getService(FriendService::class.java).isMyFriend(account)
    }
}