package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.databinding.AdapterChatlistBinding
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

class ChatListAdapter(private val beans: ArrayList<RecentContact>, private val eventImpl: EventImpl) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val viewDataBinding = DataBindingUtil.inflate<AdapterChatlistBinding>(LayoutInflater.from(parent.context), R.layout.adapter_chatlist, parent, false)
        return ChatListViewHolder(viewDataBinding)
    }

    override fun getItemCount() = beans.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.vd.setVariable(BR.recentContact, beans[holder.layoutPosition])
        holder.vd.setVariable(BR.eventImpl, eventImpl)
        holder.vd.executePendingBindings()

        holder.badge!!.badgeNumber = beans[holder.layoutPosition].unreadCount
    }

    class ChatListViewHolder(viewDataBinding: ViewDataBinding): RecyclerView.ViewHolder(viewDataBinding.root) {
        val vd = viewDataBinding

        var badge: Badge? = null

        init {
            badge = QBadgeView(viewDataBinding.root.context).bindTarget(viewDataBinding.root.findViewById(R.id.layout_adapter_conversationlist))
            badge!!.badgeGravity = Gravity.START or Gravity.TOP
            badge!!.badgeTextColor = Color.WHITE
            badge!!.badgeBackgroundColor = Color.RED
            badge!!.setGravityOffset(48f, 15f, true)
            badge!!.setBadgeTextSize(11f, true)
            badge!!.setBadgePadding(2f, true)
        }
    }
}