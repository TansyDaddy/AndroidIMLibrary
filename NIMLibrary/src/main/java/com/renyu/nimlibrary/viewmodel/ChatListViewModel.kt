package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.view.View
import com.blankj.utilcode.util.NetworkUtils
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.Resource
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ChatListAdapter
import com.renyu.nimlibrary.ui.fragment.ChatListFragment
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
     * 每个页面都要判断登录状态
     */
    fun signIn() {
        if (AuthManager.getStatus() != StatusCode.LOGINED && NetworkUtils.isConnected()) {
            AuthManager.login(UserManager.getUserAccount().first,
                    UserManager.getUserAccount().second, UserManager.getUserAccount().third == UserManager.UserRole.AGENT)
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
    }

    override fun onLongClick(view: View, recentContact: RecentContact): Boolean {
        (view.context as ChatListFragment.ChatListListener).deleteRecentContact(recentContact)
        return super.onLongClick(view, recentContact)
    }

    /**
     * 从会话列表中删除一项。
     */
    fun deleteRecentContact(contactId: String) {
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
    override fun gotoConversationActivity(view: View, recentContact: RecentContact) {
        super.gotoConversationActivity(view, recentContact)
        (view.context as ChatListFragment.ChatListListener).clickRecentContact(recentContact)
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