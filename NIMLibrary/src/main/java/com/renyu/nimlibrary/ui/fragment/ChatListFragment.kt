package com.renyu.nimlibrary.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.bean.Resource
import com.renyu.nimlibrary.bean.Status
import com.renyu.nimlibrary.databinding.FragmentChatlistBinding
import com.renyu.nimlibrary.util.OtherUtils
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.viewmodel.ChatListViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chatlist.*


class ChatListFragment : Fragment() {

    var viewDataBinding: FragmentChatlistBinding? = null

    var vm: ChatListViewModel? = null

    private var disposable: Disposable? = null

    private var chatListListener: ChatListListener? = null
    // 使用到的相关接口
    interface ChatListListener {
        // 会话列表点击
        fun clickRecentContact(recentContact: RecentContact)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        chatListListener = context as ChatListFragment.ChatListListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatlist, container, false)
        return viewDataBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.also {
            vm = ViewModelProviders.of(this).get(ChatListViewModel::class.java)
            vm?.recentContactListResponse?.observe(this, Observer<Resource<List<RecentContact>>> { t ->
                when(t?.status) {
                    Status.LOADING -> {

                    }
                    Status.SUCESS -> {
                        if (t.data != null) {
                            vm?.notifyDataSetChanged(t.data)
                        }
                    }
                    Status.FAIL -> {

                    }
                    Status.Exception -> {

                    }
                }
            })
            viewDataBinding?.adapter = vm?.adapter


            disposable = RxBus.getDefault()
                    .toObservable(ObserveResponse::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        // 在线状态
                        if (it.type == ObserveResponseType.OnlineStatus) {
                            if (it.data is StatusCode) {
                                refreshClientStatus(it.data as StatusCode)
                            }
                        }
                        if (it.type == ObserveResponseType.ObserveLoginSyncDataStatus) {
                            // 消息同步完成重新获取最近会话列表
                            vm?.queryRecentContacts()
                        }
                        // 最近会话列表变更通知
                        if (it.type == ObserveResponseType.ObserveRecentContact) {
                            vm!!.updateRecentContact(it)
                        }
                        // 用户资料变更
                        if (it.type == ObserveResponseType.UserInfoUpdate) {
                            vm!!.adapter.notifyDataSetChanged()
                        }
                        // 从服务器获取用户资料
                        if (it.type == ObserveResponseType.FetchUserInfo) {
                            vm!!.adapter.notifyDataSetChanged()
                        }
                    }
                    .subscribe()

            // 获取最近会话列表
            vm!!.queryRecentContacts()
        }
    }

    override fun onResume() {
        super.onResume()

        // 每次恢复Activity的时候都要刷新当前连接状态
        refreshClientStatus(NIMClient.getStatus())

        // 判断是否登录，没有登录自动执行登录
        vm!!.signIn()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }

    private fun refreshClientStatus(statusCode: StatusCode) {
        tv_chatlist_tip.setOnClickListener(null)
        when(statusCode) {
            // 如果用户登录成功，则同步数据
            StatusCode.LOGINED -> {
                if (OtherUtils.areNotificationsEnabled(activity)) {
                    updateTip("登录成功", false)
                }
                else {
                    updateTip("消息通知未打开", true)
                    tv_chatlist_tip.setOnClickListener {
                        OtherUtils.goToNotificationSet(activity)
                    }
                }
            }
            StatusCode.UNLOGIN -> { updateTip("未登录", true) }
            StatusCode.NET_BROKEN -> { updateTip("网络连接已断开", true) }
            StatusCode.CONNECTING -> { updateTip("正在连接服务器", true) }
            StatusCode.LOGINING -> { updateTip("正在登录中", true) }
            StatusCode.FORBIDDEN -> { updateTip("被服务器禁止登录", true) }
            StatusCode.VER_ERROR -> { updateTip("客户端版本错误", true) }
            StatusCode.PWD_ERROR -> { updateTip("用户名或密码错误", true) }
            else -> { updateTip("", false) }
        }
    }

    /**
     * 更新提示消息
     */
    private fun updateTip(text: String, visibility: Boolean) {
        tv_chatlist_tip.text = text
        tv_chatlist_tip.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}