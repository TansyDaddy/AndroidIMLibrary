package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.ViewModel
import android.view.View
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.FriendManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.adapter.ContactAdapter
import com.renyu.nimlibrary.ui.fragment.ContactFragment
import com.renyu.nimlibrary.util.PinyinComparator
import com.renyu.nimlibrary.util.RxBus
import java.util.*
import kotlin.collections.HashMap

class ContactViewModel : ViewModel(), EventImpl {

    private val userInfos: ArrayList<Any> by lazy {
        ArrayList<Any>()
    }

    val adapter: ContactAdapter by lazy {
        ContactAdapter(userInfos, this)
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
        accounts.remove(UserManager.getUserAccount().first)
        userInfos.clear()
        prepareData(UserManager.getUserInfoList(accounts))
        adapter.notifyDataSetChanged()
    }

    /**
     * 数据排序
     */
    private fun prepareData(nimUserInfos: List<NimUserInfo>) {
        val star = ArrayList<NimUserInfo>()
        // 查找出星标用户
        nimUserInfos.filter {
            FriendManager.getFriendByAccount(it.account).extension != null &&
                    FriendManager.getFriendByAccount(it.account).extension.containsKey(CommonParams.STAR) &&
                    FriendManager.getFriendByAccount(it.account).extension[CommonParams.STAR] == "1"
        }.forEach {
            star.add(it)
        }
        userInfos.clear()
        if (star.size > 0) {
            userInfos.add("星标联系人 (${star.size})")
            Collections.sort(star, PinyinComparator())
            userInfos.addAll(star)
        }
        if (nimUserInfos.isNotEmpty()) {
            userInfos.add("全部联系人 (${nimUserInfos.size})")
            Collections.sort(nimUserInfos, PinyinComparator())
            userInfos.addAll(nimUserInfos)
        }
    }

    /**
     * 更新好友列表
     */
    fun updateFriends(notify: FriendChangedNotify) {
        // 返回增加或者发生变更的好友关系
        val addedOrUpdatedFriends = notify.addedOrUpdatedFriends
        // 返回被删除的的好友关系
        val deletedFriendAccounts = notify.deletedFriends

        val temp: ArrayList<NimUserInfo> = ArrayList()
        userInfos.filter {
            it is NimUserInfo
        }.forEach {
            if (!temp.contains(it)) {
                temp.add(it as NimUserInfo)
            }
        }

        // 删除不是好友的账号
        val deleteFriends = ArrayList<NimUserInfo>()
        temp.forEach {
            if (deletedFriendAccounts.contains(it.account)) {
                deleteFriends.add(it)
            }
        }
        temp.removeAll(deleteFriends)

        // 去除需要更新的老账号
        val sameFriends = ArrayList<NimUserInfo>()
        addedOrUpdatedFriends.forEach {
            temp.forEach find@ { userInfo ->
                if (userInfo.account == it.account) {
                    sameFriends.add(userInfo)
                    return@find
                }
            }
        }
        temp.removeAll(sameFriends)

        // 添加新好友
        addedOrUpdatedFriends.forEach {
            val userInfo = UserManager.getUserInfo(it.account)
            // 本地缓存中有好友信息，则直接获取，否则从云端获取
            if (userInfo != null) {
                temp.add(userInfo)
            }
            else {
                addNewFriendInfoByNetWork(it.account)
            }
        }

        userInfos.clear()
        prepareData(temp)

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

        val temp: ArrayList<NimUserInfo> = ArrayList()
        userInfos.filter {
            it is NimUserInfo
        }.forEach {
            temp.add(it as NimUserInfo)
        }

        // 删除添加到黑名单的账号
        val deleteFriends = ArrayList<NimUserInfo>()
        temp.filter {
            addedAccounts.contains(it.account)
        }.forEach {
            deleteFriends.add(it)
        }
        temp.removeAll(deleteFriends)

        // 添加移出黑名单的账号
        removedAccounts.forEach {
            val userInfo = UserManager.getUserInfo(it)
            // 本地缓存中有好友信息，则直接获取，否则从云端获取
            if (userInfo != null) {
                temp.add(userInfo)
            }
            else {
                addNewFriendInfoByNetWork(it)
            }
        }

        userInfos.clear()
        prepareData(temp)

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
                    RxBus.getDefault().post(ObserveResponse(param, ObserveResponseType.FetchUserInfo))
                    if (param != null) {
                        val temp: ArrayList<NimUserInfo> = ArrayList()
                        userInfos.filter {
                            it is NimUserInfo
                        }.forEach {
                            temp.add(it as NimUserInfo)
                        }
                        temp.addAll(param)

                        userInfos.clear()
                        prepareData(temp)

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

    /**
     * 打开个人详情
     */
    override fun gotoUserInfo(view: View, account: String) {
        super.gotoUserInfo(view, account)
        (view.context as ContactFragment.ContactListener).gotoUserInfo(account)
    }

    /**
     * 联系人列表点击
     */
    override fun clickContact(view: View, nimUserInfo: NimUserInfo) {
        super.clickContact(view, nimUserInfo)
        (view.context as ContactFragment.ContactListener).clickContact(nimUserInfo)
    }

    override fun clickStar(view: View, nimUserInfo: NimUserInfo) {
        super.clickStar(view, nimUserInfo)
        // 已经是星标联系人
        val star = if (FriendManager.getFriendByAccount(nimUserInfo.account).extension != null &&
                FriendManager.getFriendByAccount(nimUserInfo.account).extension.containsKey(CommonParams.STAR) &&
                FriendManager.getFriendByAccount(nimUserInfo.account).extension[CommonParams.STAR] == "1") {
            "0"
        }
        else {
            "1"
        }
        val map = HashMap<String, String>()
        map[CommonParams.STAR] = star
        FriendManager.updateFriendFields(nimUserInfo.account, map, object : RequestCallback<Void> {
            override fun onSuccess(param: Void?) {

            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable?) {

            }
        })
    }
}