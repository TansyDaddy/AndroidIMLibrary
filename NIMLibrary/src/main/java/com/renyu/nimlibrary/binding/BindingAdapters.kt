package com.renyu.nimlibrary.binding

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState
import com.netease.nimlib.sdk.avchat.constant.AVChatType
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.uinfo.UserService
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.extension.StickerAttachment
import com.renyu.nimlibrary.extension.VRAttachment
import com.renyu.nimlibrary.util.OtherUtils
import com.renyu.nimlibrary.util.emoji.EmojiUtils
import com.renyu.nimlibrary.util.sticker.StickerUtils
import java.io.File

object BindingAdapters {
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T: RecyclerView.ViewHolder> setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<T>) {
        recyclerView.setHasFixedSize(true)
        val manager = LinearLayoutManager(recyclerView.context)
        manager.isSmoothScrollbarEnabled = true
        manager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = manager
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter(value = ["avatarImage"])
    fun loadAvatarImage(simpleDraweeView: SimpleDraweeView, account: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(account)
        if (userInfo != null) {
            if (simpleDraweeView.tag !=null &&
                    !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                    simpleDraweeView.tag.toString() == userInfo.avatar) {
                // 什么都不做，防止Fresco闪烁
            }
            else {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(userInfo.avatar))
                        .setResizeOptions(ResizeOptions(SizeUtils.dp2px(40f), SizeUtils.dp2px(40f))).build()
                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request).setAutoPlayAnimations(true).build()
                simpleDraweeView.controller = draweeController
                simpleDraweeView.tag = userInfo.avatar
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["showUnreadNum"])
    fun loadUnreadNum(textView: TextView, count: Int) {
        textView.visibility = if (count>0) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["chatName"])
    fun loadChatName(textView: TextView, contactId: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(contactId)
        if (userInfo != null) {
            textView.text = userInfo.name
        }
        else {
            textView.text = contactId
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["emojiText"])
    fun loadEmojiText(textView: SimpleDraweeSpanTextView, text: String) {
        EmojiUtils.replaceFaceMsgByFresco(textView, text)
    }

    @JvmStatic
    @BindingAdapter(value = ["emojiTextWithAttachment", "attachment"])
    fun loadEmojiTextWithAttachment(textView: SimpleDraweeSpanTextView, text: String, attachment: MsgAttachment?) {
        // 如果是自定义消息中的贴图消息
        if (attachment != null && attachment is StickerAttachment) {
            textView.text = "[贴图]"
            return
        }
        // 如果是自定义消息中的VR消息
        if (attachment != null && attachment is VRAttachment) {
            textView.text = "[VR]"
            return
        }
        EmojiUtils.replaceFaceMsgByFresco(textView, text)
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListPbStatue"])
    fun changeConversationListProgressStatue(progressBar: ProgressBar, statue: Int) {
        if (statue == MsgStatusEnum.sending.value) {
            // 发送中
            progressBar.visibility = View.VISIBLE
        }
        else {
            progressBar.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListIvStatue"])
    fun changeConversationListImageStatue(imageView: ImageView, statue: Int) {
        if (statue == MsgStatusEnum.fail.value) {
            // 发送失败
            imageView.visibility = View.VISIBLE
        }
        else {
            imageView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListAudiolength"])
    fun changeConversationListAudiolength(textView: TextView, imMessage: IMMessage) {
        val duration = (imMessage.attachment as AudioAttachment).duration / 1000
        textView.text = "$duration\'\'"
    }

    @JvmStatic
    @BindingAdapter(value = ["read"])
    fun changeReadedVisibility(textView: TextView, imMessage: IMMessage) {
        if (imMessage.sessionType == SessionTypeEnum.P2P
                && imMessage.direct == MsgDirectionEnum.Out
                && imMessage.msgType != MsgTypeEnum.tip
                && imMessage.msgType != MsgTypeEnum.notification
                && imMessage.isRemoteRead) {
            textView.visibility = View.VISIBLE
        }
        else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["audioRead"])
    fun changeConversationListAudioRead(imageView: ImageView, msgStatusEnum: MsgStatusEnum) {
        if (msgStatusEnum == MsgStatusEnum.success) {
            imageView.visibility = View.VISIBLE
        }
        else {
            imageView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListImageUrl"])
    fun loadChatListImageUrl(simpleDraweeView: SimpleDraweeView, imMessage: IMMessage) {
        val imageUrl = if ((imMessage.attachment as ImageAttachment).path == null) {
            if ((imMessage.attachment as ImageAttachment).thumbUrl == null) {
                ""
            }
            else {
                (imMessage.attachment as ImageAttachment).thumbUrl
            }
        }
        else {
            val file = File((imMessage.attachment as ImageAttachment).path)
            if (file.exists()) {
                "file://"+(imMessage.attachment as ImageAttachment).path
            }
            else {
                ""
            }
        }
        if (simpleDraweeView.tag !=null &&
                !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                simpleDraweeView.tag.toString() == imageUrl) {
            // 什么都不做，防止Fresco闪烁
        }
        else {
            val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(123f), SizeUtils.dp2px(115f))).build()
            val draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request).setAutoPlayAnimations(true).build()
            simpleDraweeView.controller = draweeController
            simpleDraweeView.tag = imageUrl
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListSticker"])
    fun loadChatListSticker(simpleDraweeView: SimpleDraweeView, imMessage: IMMessage) {
        val path = StickerUtils.getStickerUri((imMessage.attachment as StickerAttachment).catalog,
                (imMessage.attachment as StickerAttachment).chartlet)

        if (simpleDraweeView.tag !=null &&
                !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                simpleDraweeView.tag.toString() == path) {
            // 什么都不做，防止Fresco闪烁
        }
        else {
            val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(123f), SizeUtils.dp2px(115f))).build()
            val draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request).setAutoPlayAnimations(true).build()
            simpleDraweeView.controller = draweeController
            simpleDraweeView.tag = path
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListAvChatImage"])
    fun loadChatListAvChatImage(imageView: ImageView, imMessage: IMMessage) {
        if ((imMessage.attachment as AVChatAttachment).type == AVChatType.AUDIO) {
            imageView.setImageResource(R.mipmap.avchat_left_type_audio)
        }
        else {
            imageView.setImageResource(R.mipmap.avchat_left_type_video)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListAvChatText"])
    fun loadChatListAvChatImage(textView: TextView, imMessage: IMMessage) {
        val attachment = imMessage.attachment as AVChatAttachment
        textView.text = when (attachment.state) {
            //成功接听
            AVChatRecordState.Success -> OtherUtils.secToTime(attachment.duration)
            //未接听
            AVChatRecordState.Missed -> "未接听"
            //主动拒绝
            AVChatRecordState.Rejected -> "已挂断"
            else -> ""
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListVRImage"])
    fun loadChatListVRImage(simpleDraweeView: SimpleDraweeView, imMessage: IMMessage) {
        val attachment = imMessage.attachment as VRAttachment
        if (simpleDraweeView.tag !=null &&
                !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                simpleDraweeView.tag.toString() == attachment.vrJson) {
            // 什么都不做，防止Fresco闪烁
        }
        else {
            val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(attachment.vrJson))
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(123f), SizeUtils.dp2px(115f))).build()
            val draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request).setAutoPlayAnimations(true).build()
            simpleDraweeView.controller = draweeController
            simpleDraweeView.tag = attachment.vrJson
        }
    }
}