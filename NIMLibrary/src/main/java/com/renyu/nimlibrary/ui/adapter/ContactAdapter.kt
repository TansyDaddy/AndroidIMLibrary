package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.databinding.AdapterContactBinding

class ContactAdapter(val beans: ArrayList<NimUserInfo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewDataBinding = DataBindingUtil.inflate<AdapterContactBinding>(LayoutInflater.from(parent.context), R.layout.adapter_contact, parent, false)
        return FriendlistViewHolder(viewDataBinding)
    }

    override fun getItemCount() = beans.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FriendlistViewHolder).vd.setVariable(BR.nimUserInfo, beans[holder.layoutPosition])
        holder.vd.executePendingBindings()
    }

    class FriendlistViewHolder(val viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        val vd = viewDataBinding
    }
}