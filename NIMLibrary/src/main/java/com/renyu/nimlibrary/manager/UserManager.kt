package com.renyu.nimlibrary.manager

import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.UserServiceObserve
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.params.CommonParams
import java.text.SimpleDateFormat
import java.util.*

object UserManager {

    /**
     * 从本地数据库中批量获取用户资料
     */
    fun getUserInfoList(accounts: List<String>): List<NimUserInfo> {
        if (accounts.isEmpty()) {
            return ArrayList()
        }
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

    /**
     * 设置用户信息
     */
    fun setUserAccount(accid: String?, token: String?, userRole: UserRole = UserRole.UNSPECIFIED) {
        if (TextUtils.isEmpty(accid) || TextUtils.isEmpty(token)) {
            // 清除用户登录信息
            SPUtils.getInstance().remove(CommonParams.SP_ACCID)
            SPUtils.getInstance().remove(CommonParams.SP_TOKEN)
        }
        else {
            SPUtils.getInstance().put(CommonParams.SP_ACCID, accid)
            SPUtils.getInstance().put(CommonParams.SP_TOKEN, token)
        }
        SPUtils.getInstance().put(CommonParams.SP_USERROLE, userRole.role)
    }

    /**
     * 获取用户信息
     */
    @JvmStatic
    fun getUserAccount(): Triple<String, String, UserRole> {
        val accid = SPUtils.getInstance().getString(CommonParams.SP_ACCID)
        val token = SPUtils.getInstance().getString(CommonParams.SP_TOKEN)
        val userRole = when(SPUtils.getInstance().getInt(CommonParams.SP_USERROLE)) {
            1 -> UserRole.AGENT
            2 -> UserRole.CUSTOMER
            else -> UserRole.UNSPECIFIED
        }
        return Triple(accid, token, userRole)
    }

    /**
     * 用户类型
     */
    enum class UserRole(var role: Int) {
        // 未知
        UNSPECIFIED(0),
        // 经纪人
        AGENT(1),
        // 客户
        CUSTOMER(2)
    }

    /**
     * 设置最后一次登录日期
     */
    fun setLastSignInTime() {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        SPUtils.getInstance().put(CommonParams.SP_LASTSIGNIN, simpleDateFormat.format(Date()))
    }

    /**
     * 判断今天是否登录过
     */
    fun isTodaySignIn(): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(Date()) == SPUtils.getInstance().getString(CommonParams.SP_LASTSIGNIN)
    }
}