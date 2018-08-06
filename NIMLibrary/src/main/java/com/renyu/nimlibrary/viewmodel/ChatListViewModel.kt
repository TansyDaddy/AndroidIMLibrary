package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.view.View
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.UserService
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ChatListAdapter
import java.util.*
import kotlin.collections.ArrayList

class ChatListViewModel : ViewModel(), EventImpl {

    // 置顶功能可直接使用，也可作为思路，供开发者充分利用RecentContact的tag字段
    private val RECENT_TAG_STICKY: Long = 1 // 联系人置顶tag

    private val beans: ArrayList<RecentContact> by lazy {
        ArrayList<RecentContact>()
    }

    // 用来设置的adapter
    val adapter: ChatListAdapter by lazy {
        ChatListAdapter(beans, this)
    }

    // 接口请求数据
    private val recentContactListRequest = MutableLiveData<String>()
    var recentContactListResponse: LiveData<Resource<List<RecentContact>>>? = null

    init {
        recentContactListResponse = Transformations.switchMap(recentContactListRequest) {
            if (it == null) {
                MutableLiveData()
            }
            else {
                Repos.queryRecentContacts()
            }
        }
    }

    /**
     * 获取会话列表
     */
    fun queryRecentContacts() {
        recentContactListRequest.value = "${System.currentTimeMillis()}"
    }

    /**
     * 刷新数据
     */
    fun notifyDataSetChanged(recentContacts: List<RecentContact>) {
        beans.clear()
        beans.addAll(sortRecentContacts(recentContacts))
        adapter.notifyDataSetChanged()

        // 刷新用户个人数据
        refreshUserInfo()
    }

    /**
     * 会话列表变更通知
     */
    fun updateRecentContact(recentContacts: ObserveResponse) {
        (recentContacts.data as List<*>).forEach {
            if (it is RecentContact) {
                var indexTemp = -1
                // 找到同一个Item并删除
                beans.forEachIndexed { index, recentContact ->
                    if (recentContact.contactId == it.contactId &&
                            recentContact.sessionType == it.sessionType) {
                        indexTemp = index
                        return@forEachIndexed
                    }
                }
                if (indexTemp != -1) {
                    beans.removeAt(indexTemp)
                }
                // 添加当前Item
                beans.add(it)
            }
        }
        sortRecentContacts(beans)
        adapter.notifyDataSetChanged()

        // 刷新用户个人数据
        refreshUserInfo()
    }

    /**
     * 从会话列表中删除一项。
     */
    override fun deleteRecentContact(view: View, contactId: String) {
        super.deleteRecentContact(view, contactId)

        beans.filter {
            it.contactId == contactId
        }.forEach {
            MessageManager.deleteRecentContact(it)
            beans.remove(it)
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 跳转会话详情
     */
    override fun gotoConversationActivity(view: View, account: String) {
        super.gotoConversationActivity(view, account)
        try {
            val clazz = Class.forName("com.renyu.nimapp.params.InitParams")
            val conversationActivityName = clazz.getField("ConversationActivityName").get(clazz).toString()
            val conversationClass = Class.forName(conversationActivityName)

            val intent = Intent(view.context, conversationClass)
            intent.putExtra("account", account)
            intent.putExtra("isGroup", false)
            view.context.startActivity(intent)
        }
        catch (e:ClassNotFoundException) {
            e.printStackTrace()
        }
        catch (e:IllegalAccessException) {
            e.printStackTrace()
        }
        catch (e:NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    /**
     * 刷新用户个人数据
     */
    private fun refreshUserInfo() {
        val refreshLists = ArrayList<String>()
        beans.forEach {
            val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(it.contactId)
            if (userInfo == null) {
                refreshLists.add(it.contactId)
            }
        }
        if (refreshLists.size>0) {
            UserManager.fetchUserInfo(refreshLists)
        }
    }

    private fun sortRecentContacts(list: List<RecentContact>):  List<RecentContact> {
        if (!list.isEmpty()) {
            Collections.sort(list, comp)
        }
        return list
    }

    private val comp = Comparator<RecentContact> { o1, o2 ->
        // 先比较置顶tag
        val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
        if (sticky != 0L) {
            if (sticky > 0) -1 else 1
        } else {
            val time = o1.time - o2.time
            if (time == 0L) 0 else if (time > 0) -1 else 1
        }
    }
}