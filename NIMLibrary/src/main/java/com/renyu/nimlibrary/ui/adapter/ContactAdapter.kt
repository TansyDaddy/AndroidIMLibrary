package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.databinding.AdapterContactBinding
import com.renyu.nimlibrary.databinding.AdapterContacttitleBinding

class ContactAdapter(private val beans: ArrayList<Any>, private val eventImpl: EventImpl) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TITLE = 0
    private val CONTENT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            CONTENT -> {
                val viewDataBinding = DataBindingUtil.inflate<AdapterContactBinding>(LayoutInflater.from(parent.context), R.layout.adapter_contact, parent, false)
                return FriendlistViewHolder(viewDataBinding)
            }
            TITLE -> {
                val viewDataBinding = DataBindingUtil.inflate<AdapterContacttitleBinding>(LayoutInflater.from(parent.context), R.layout.adapter_contacttitle, parent, false)
                return FriendlistTitleViewHolder(viewDataBinding)
            }
        }
        throw Throwable("对指定viewType类型缺少判断")
    }

    override fun getItemCount() = beans.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TITLE) {
            (holder as FriendlistTitleViewHolder).vd.setVariable(BR.desp, beans[holder.layoutPosition])
            holder.vd.setVariable(BR.eventImpl, eventImpl)
            holder.vd.executePendingBindings()
        }
        else if (getItemViewType(position) == CONTENT) {
            (holder as FriendlistViewHolder).vd.setVariable(BR.nimUserInfo, beans[holder.layoutPosition])
            holder.vd.setVariable(BR.eventImpl, eventImpl)
            holder.vd.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (beans[position] is NimUserInfo) {
            CONTENT
        }
        else {
            TITLE
        }
    }

    class FriendlistViewHolder(val viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        val vd = viewDataBinding
    }

    class FriendlistTitleViewHolder(val viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        val vd = viewDataBinding
    }
}