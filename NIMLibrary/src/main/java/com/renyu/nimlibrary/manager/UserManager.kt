package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.UserServiceObserve
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo

object UserManager {

    /**
     * 从本地数据库中批量获取用户资料
     */
    fun getUserInfoList(accounts: List<String>): List<NimUserInfo> {
        return NIMClient.getService(UserService::class.java).getUserInfoList(accounts)

    }

    /**
     * 从本地数据库中获取用户资料
     */
    fun getUserInfo(account: String): NimUserInfo? {
        return NIMClient.getService(UserService::class.java).getUserInfo(account)
    }

    /**
     * 获取本地数据库中所有用户资料
     */
    fun getAllUserInfo(): List<NimUserInfo> {
        return NIMClient.getService(UserService::class.java).allUserInfo
    }

    /**
     * 监听用户资料变更
     */
    fun observeUserInfoUpdate() {
        NIMClient.getService(UserServiceObserve::class.java).observeUserInfoUpdate({
            it.forEach {
                Log.d("NIM_APP", "用户资料变更通知：${it.name}")
            }
        }, true)
    }

    /**
     * 从服务器获取用户资料
     */
    fun fetchUserInfo(accounts: List<String>, callback: RequestCallback<List<NimUserInfo>>) {
        NIMClient.getService(UserService::class.java).fetchUserInfo(accounts).setCallback(callback)
    }
}