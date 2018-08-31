package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.databinding.library.baseAdapters.BR
import com.blankj.utilcode.util.ScreenUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.databinding.*
import com.renyu.nimlibrary.extension.CustomAttachment
import com.renyu.nimlibrary.extension.CustomAttachmentType
import com.renyu.nimlibrary.ui.viewholder.*
import com.renyu.nimlibrary.util.OtherUtils
import com.renyu.nimlibrary.util.audio.MessageAudioControl

class ConversationAdapter(val messages: ArrayList<IMMessage>, private val eventImpl: EventImpl) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            0 -> return TextViewHolder(
                        DataBindingUtil.inflate<AdapterReceiveTextBinding>(
                                LayoutInflater.from(parent.context),
                                R.layout.adapter_receive_text, parent,
                                false))
            1 -> return TextViewHolder(
                    DataBindingUtil.inflate<AdapterSendTextBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_text, parent,
                            false))
            2 -> return ImageViewHolder(
                    DataBindingUtil.inflate<AdapterReceivePhotoBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_photo, parent,
                            false))
            3 -> return ImageViewHolder(
                    DataBindingUtil.inflate<AdapterSendPhotoBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_photo, parent,
                            false))
            4 -> return AudioViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveVoiceBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_voice, parent,
                            false), this)
            5 -> return AudioViewHolder(
                    DataBindingUtil.inflate<AdapterSendVoiceBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_voice, parent,
                            false), this)
            8 -> return LocationViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveLocationBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_location, parent,
                            false))
            9 -> return LocationViewHolder(
                    DataBindingUtil.inflate<AdapterSendLocationBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_location, parent,
                            false))
            12 -> return AVChatViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveAvchatBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_avchat, parent,
                            false))
            13 -> return AVChatViewHolder(
                    DataBindingUtil.inflate<AdapterSendAvchatBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_avchat, parent,
                            false))
            16, 17 -> return TipHolder(
                    DataBindingUtil.inflate<AdapterTipBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_tip, parent,
                            false))
            18 -> return StickerViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveStickerBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_sticker, parent,
                            false))
            19 -> return StickerViewHolder(
                    DataBindingUtil.inflate<AdapterSendStickerBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_sticker, parent,
                            false))
            20 -> return VRViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveVrBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_vr, parent,
                            false))
            21 -> return VRViewHolder(
                    DataBindingUtil.inflate<AdapterSendVrBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_vr, parent,
                            false))
            22 -> return HouseCardViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveHousecardBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_housecard, parent,
                            false))
            23 -> return HouseCardViewHolder(
                    DataBindingUtil.inflate<AdapterSendHousecardBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_housecard, parent,
                            false))
            24 -> return UserInfoViewHolder(
                    DataBindingUtil.inflate<AdapterReceiveUserinfoBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_receive_userinfo, parent,
                            false))
            25 -> return UserInfoViewHolder(
                    DataBindingUtil.inflate<AdapterSendUserinfoBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_userinfo, parent,
                            false))
        }
        throw Throwable("对指定viewType类型缺少判断")
    }

    override fun getItemCount() = messages.size

    private fun initViewDataBinding(viewDataBinding: ViewDataBinding, position: Int) {
        viewDataBinding.setVariable(BR.imMessage, messages[position])
        viewDataBinding.setVariable(BR.eventImpl, eventImpl)
        viewDataBinding.executePendingBindings()
        // 调整时间显示
        modifyShowTime(position, viewDataBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(holder.layoutPosition)) {
            0, 1 -> {
                initViewDataBinding((holder as TextViewHolder).tvDataBinding, holder.layoutPosition)
            }
            2, 3 -> {
                initViewDataBinding((holder as ImageViewHolder).ivDataBinding, holder.layoutPosition)
                calculateBubbleSize(messages[holder.layoutPosition], holder.ivDataBinding.root.findViewById<SimpleDraweeView>(R.id.aurora_iv_msgitem_photo))
            }
            4, 5 -> {
                initViewDataBinding((holder as AudioViewHolder).audioDataBinding, holder.layoutPosition)
                calculateBubbleWidth((messages[holder.layoutPosition].attachment as AudioAttachment).duration, holder.audioDataBinding.root.findViewById<RelativeLayout>(R.id.bubble))
                holder.imMessage = messages[holder.layoutPosition]
                holder.audioControl = MessageAudioControl.getInstance()
                holder.changeUI()
            }
            8, 9 -> {
                initViewDataBinding((holder as LocationViewHolder).locationDataBinding, holder.layoutPosition)
            }
            12, 13 -> {
                initViewDataBinding((holder as AVChatViewHolder).avDataBinding, holder.layoutPosition)
            }
            16, 17 -> {
                (holder as TipHolder).tipDataBinding.setVariable(BR.imMessage, messages[holder.layoutPosition])
                // 调整时间显示
                modifyShowTime(position, holder.tipDataBinding)
            }
            18, 19 -> {
                if (messages[holder.layoutPosition].attachment != null) {
                    when((messages[holder.layoutPosition].attachment as CustomAttachment).type) {
                        CustomAttachmentType.Sticker -> initViewDataBinding((holder as StickerViewHolder).ivDataBinding, holder.layoutPosition)
                    }
                }
            }
            20, 21, 22, 23, 24, 25 -> {
                if (messages[holder.layoutPosition].attachment != null) {
                    when((messages[holder.layoutPosition].attachment as CustomAttachment).type) {
                        CustomAttachmentType.VR -> initViewDataBinding((holder as VRViewHolder).ivDataBinding, holder.layoutPosition)
                        CustomAttachmentType.HOUSE -> initViewDataBinding((holder as HouseCardViewHolder).ivDataBinding, holder.layoutPosition)
                        CustomAttachmentType.USERINFO -> initViewDataBinding((holder as UserInfoViewHolder).ivDataBinding, holder.layoutPosition)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 接收文字消息
        if (messages[position].msgType == MsgTypeEnum.text && messages[position].direct == MsgDirectionEnum.In) {
            return 0
        }
        // 发送文字消息
        else if (messages[position].msgType == MsgTypeEnum.text && messages[position].direct == MsgDirectionEnum.Out) {
            return 1
        }
        // 接收图片消息
        if (messages[position].msgType == MsgTypeEnum.image && messages[position].direct == MsgDirectionEnum.In) {
            return 2
        }
        // 发送图片消息
        else if (messages[position].msgType == MsgTypeEnum.image && messages[position].direct == MsgDirectionEnum.Out) {
            return 3
        }
        // 接收音频消息
        if (messages[position].msgType == MsgTypeEnum.audio && messages[position].direct == MsgDirectionEnum.In) {
            return 4
        }
        // 发送音频消息
        else if (messages[position].msgType == MsgTypeEnum.audio && messages[position].direct == MsgDirectionEnum.Out) {
            return 5
        }
        // 接收视频消息
        if (messages[position].msgType == MsgTypeEnum.video && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送视频消息
        else if (messages[position].msgType == MsgTypeEnum.video && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收位置消息
        if (messages[position].msgType == MsgTypeEnum.location && messages[position].direct == MsgDirectionEnum.In) {
            return 8
        }
        // 发送位置消息
        else if (messages[position].msgType == MsgTypeEnum.location && messages[position].direct == MsgDirectionEnum.Out) {
            return 9
        }
        // 接收文件消息
        if (messages[position].msgType == MsgTypeEnum.file && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送文件消息
        else if (messages[position].msgType == MsgTypeEnum.file && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收音视频消息
        if (messages[position].msgType == MsgTypeEnum.avchat && messages[position].direct == MsgDirectionEnum.In) {
            return 12
        }
        // 发送音视频消息
        else if (messages[position].msgType == MsgTypeEnum.avchat && messages[position].direct == MsgDirectionEnum.Out) {
            return 13
        }
        // 接收通知消息
        if (messages[position].msgType == MsgTypeEnum.notification && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送通知消息
        else if (messages[position].msgType == MsgTypeEnum.notification && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收提醒类型消息
        if (messages[position].msgType == MsgTypeEnum.tip && messages[position].direct == MsgDirectionEnum.In) {
            return 16
        }
        // 发送提醒类型消息
        else if (messages[position].msgType == MsgTypeEnum.tip && messages[position].direct == MsgDirectionEnum.Out) {
            return 17
        }
        // 接收自定义消息
        if (messages[position].msgType == MsgTypeEnum.custom && messages[position].direct == MsgDirectionEnum.In) {
            if (messages[position].attachment != null) {
                when((messages[position].attachment as CustomAttachment).type) {
                    CustomAttachmentType.Sticker -> return 18
                    CustomAttachmentType.VR -> return 20
                    CustomAttachmentType.HOUSE -> return 22
                    CustomAttachmentType.USERINFO -> return 24
                }
            }
        }
        // 发送自定义消息
        else if (messages[position].msgType == MsgTypeEnum.custom && messages[position].direct == MsgDirectionEnum.Out) {
            if (messages[position].attachment != null) {
                when((messages[position].attachment as CustomAttachment).type) {
                    CustomAttachmentType.Sticker -> return 19
                    CustomAttachmentType.VR -> return 21
                    CustomAttachmentType.HOUSE -> return 23
                    CustomAttachmentType.USERINFO -> return 25
                }
            }
        }
        return super.getItemViewType(position)
    }

    private fun modifyShowTime(position: Int, viewDataBinding: ViewDataBinding) {
        if (timedItems.contains(messages[position].uuid)) {
            viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.VISIBLE
        }
        else {
            viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.GONE
        }
    }

    // 需要显示消息时间的消息ID
    private val timedItems: MutableSet<String> by lazy {
        LinkedHashSet<String>()
    }
    // 用于消息时间显示,判断和上条消息间的时间间隔
    private var lastShowTimeItem: IMMessage? = null

    /**
     * 列表加入新消息时，更新时间显示
     */
    fun updateShowTimeItem(items: List<IMMessage>, fromStart: Boolean, update: Boolean) {
        var anchor = if (fromStart) null else lastShowTimeItem
        for (message in items) {
            // 如果是lastShowTimeItem发生改变，则只判断改变之后的部分
            if (!fromStart && anchor!=null && message.time < anchor.time) {
                continue
            }
            // 对比下一条数据
            if (setShowTimeFlag(message, anchor)) {
                anchor = message
            }
        }
        // 添加旧数据的时候无需刷新lastShowTimeItem
        if (update) {
            lastShowTimeItem = anchor
        }
    }

    /**
     * 是否需要显示时间
     */
    private fun needShowTime(message: IMMessage): Boolean {
        return timedItems.contains(message.uuid)
    }

    /**
     * 设置显示时间列表
     */
    private fun setShowTime(message: IMMessage, show: Boolean) {
        if (show) {
            timedItems.add(message.uuid)
        } else {
            timedItems.remove(message.uuid)
        }
    }

    private fun setShowTimeFlag(message: IMMessage, anchor: IMMessage?): Boolean {
        var update = false
        // 添加第一条消息时间
        if (anchor == null) {
            setShowTime(message, true)
            update = true
        }
        else {
            val messageTime = message.time
            val anchorTime = anchor.time
            if (messageTime - anchorTime == 0.toLong()) {
                setShowTime(message, true)
                lastShowTimeItem = message
                update = true
            }
            else if (messageTime - anchorTime < 5 * 60 * 1000) {
                // 去除之前可能已经存在的
                setShowTime(message, false)
            }
            else {
                // 时间间隔大于5分钟，则添加
                setShowTime(message, true)
                update = true
            }
        }
        return update
    }

    /**
     * 由于消息被删除而重新调整显示的时间
     */
    fun relocateShowTimeItemAfterDelete(messageItem: IMMessage, index: Int, messages: ArrayList<IMMessage>) {
        if (needShowTime(messageItem)) {
            setShowTime(messageItem, false)
            if (messages.size > 0) {
                var nextItem: IMMessage? = null
                // 向上寻找最接近的一个lastShowTimeItem作为新的lastShowTimeItem
                for (i in index-1 downTo 0) {
                    if (timedItems.contains(messages[i].uuid)) {
                        nextItem = messages[i]
                        break
                    }
                }
                lastShowTimeItem = nextItem
            } else {
                lastShowTimeItem = null
            }
        }
    }

    /**
     * 设置音频气泡的宽度
     */
    private fun calculateBubbleWidth(duration: Long, view: View) {
        val seconds = OtherUtils.getSecondsByMilliseconds(duration)

        val maxAudioBubbleWidth = (0.6 * ScreenUtils.getScreenWidth()).toInt()
        val minAudioBubbleWidth = (0.1875 * ScreenUtils.getScreenWidth()).toInt()
        var currentBubbleWidth = when {
            seconds <= 0 -> minAudioBubbleWidth
            seconds in 1..60 -> (((maxAudioBubbleWidth - minAudioBubbleWidth).toDouble() * (2.0 / Math.PI)
                    * Math.atan(seconds / 10.0)) + minAudioBubbleWidth).toInt()
            else -> maxAudioBubbleWidth
        }
        if (currentBubbleWidth < minAudioBubbleWidth) {
            currentBubbleWidth = minAudioBubbleWidth
        } else if (currentBubbleWidth > maxAudioBubbleWidth) {
            currentBubbleWidth = maxAudioBubbleWidth
        }

        val layoutParams = view.layoutParams
        layoutParams.width = currentBubbleWidth
        view.layoutParams = layoutParams
    }

    /**
     * 设置图片的宽高
     */
    private fun calculateBubbleSize(imMessage: IMMessage, view: View) {
        val imageSize = OtherUtils.getImageSize(imMessage)
        val maskParams = view.layoutParams
        maskParams.width = imageSize.width
        maskParams.height = imageSize.height
        view.layoutParams = maskParams
    }
}