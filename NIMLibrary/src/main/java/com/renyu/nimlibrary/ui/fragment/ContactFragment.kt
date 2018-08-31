package com.renyu.nimlibrary.ui.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.databinding.FragmentContactBinding
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.viewmodel.ContactViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class ContactFragment : Fragment() {

    var viewDataBinding: FragmentContactBinding? = null

    var vm: ContactViewModel? = null

    private var disposable: Disposable? = null

    private var contactListener: ContactListener? = null
    // 使用到的相关接口
    interface ContactListener {
        // 打开个人详情
        fun gotoUserInfo(account: String)
        // 联系人列表点击
        fun clickContact(nimUserInfo: NimUserInfo)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        contactListener = context as ContactListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact, container, false)
        return viewDataBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding?.also {
            vm = ViewModelProviders.of(this).get(ContactViewModel::class.java)

            viewDataBinding?.adapter = vm!!.adapter
        }

        disposable = RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    // 发生好友关系变化
                    if (it.type == ObserveResponseType.FriendChangedNotify) {
                        vm!!.updateFriends(it.data as FriendChangedNotify)
                    }
                    // 发生黑名单变更通知
                    if (it.type == ObserveResponseType.BlackListChangedNotify) {
                        vm!!.updateBlackList(it.data as BlackListChangedNotify)
                    }
                }
                .subscribe()

        vm!!.getUserInfoOfMyFriends()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable?.dispose()
    }
}