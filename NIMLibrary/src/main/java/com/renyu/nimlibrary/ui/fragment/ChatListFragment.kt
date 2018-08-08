package com.renyu.nimlibrary.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimapp.bean.Resource
import com.renyu.nimapp.bean.Status
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.databinding.FragmentChatlistBinding
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.viewmodel.ChatListViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class ChatListFragment : Fragment() {

    var viewDataBinding: FragmentChatlistBinding? = null

    var vm: ChatListViewModel? = null

    private var disposable: Disposable? = null

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

            Handler().postDelayed({
                vm?.queryRecentContacts()
            }, 250)
        }

        disposable = RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    // 在线状态
                    if (it.type == ObserveResponseType.OnlineStatus) {
                        // 如果用户登录成功，则同步数据
                        if (it.data is StatusCode && (it.data as StatusCode) == StatusCode.LOGINED) {

                        }
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
    }

    override fun onResume() {
        super.onResume()
        // 不需要通知显示
        MessageManager.enableMsgNotification(false)
    }

    override fun onPause() {
        super.onPause()
        // 需要通知显示
        MessageManager.enableMsgNotification(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable?.dispose()
    }
}