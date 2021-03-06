package com.renyu.nimlibrary.binding

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimavchatlibrary.params.AVChatTypeEnum
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.extension.*
import com.renyu.nimlibrary.manager.FriendManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.ui.view.WrapContentLinearLayoutManager
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.util.emoji.EmojiUtils
import com.renyu.nimlibrary.util.sticker.StickerUtils
import org.json.JSONObject
import java.io.File

object BindingAdapters {
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T: RecyclerView.ViewHolder> setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<T>) {
        recyclerView.setHasFixedSize(true)
        val manager = WrapContentLinearLayoutManager(recyclerView.context)
        manager.isSmoothScrollbarEnabled = true
        manager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = manager
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setItemViewCacheSize(20)
        recyclerView.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter(value = ["avatarImage"])
    fun loadAvatarImage(simpleDraweeView: SimpleDraweeView, account: String) {
        val userInfo = UserManager.getUserInfo(account)
        if (userInfo != null) {
            if (simpleDraweeView.tag !=null &&
                    !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                    simpleDraweeView.tag.toString() == userInfo.avatar) {
                // 什么都不做，防止Fresco闪烁
            }
            else {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(userInfo.avatar))
                        .setResizeOptions(ResizeOptions(SizeUtils.dp2px(51f), SizeUtils.dp2px(51f))).build()
                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request).setAutoPlayAnimations(true).build()
                simpleDraweeView.controller = draweeController
                simpleDraweeView.tag = userInfo.avatar
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["chatName"])
    fun loadChatName(textView: TextView, contactId: String) {
        val userInfo = UserManager.getUserInfo(contactId)
        if (userInfo != null) {
            textView.text = userInfo.name
        }
        else {
            textView.text = contactId
        }
    }

    /**
     * 检测用户信息是否存在，不存在的话执行云端获取操作
     */
    @JvmStatic
    @BindingAdapter(value = ["checkUserInfoExists"])
    fun checkUserInfoExists(textView: TextView, contactId: String) {
        val userInfo = UserManager.getUserInfo(contactId)
        if (userInfo == null) {
            val refreshLists = ArrayList<String>()
            refreshLists.add(contactId)
            UserManager.fetchUserInfo(refreshLists, object : RequestCallback<List<NimUserInfo>> {
                override fun onSuccess(param: List<NimUserInfo>?) {
                    if (param?.size != 0) {
                        RxBus.getDefault().post(ObserveResponse(param, ObserveResponseType.FetchUserInfo))
                        param?.forEach {
                            Log.d("NIM_APP", "从服务器获取用户资料：${it.name}")
                        }
                    }
                }

                override fun onFailed(code: Int) {

                }

                override fun onException(exception: Throwable?) {

                }
            })
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["emojiText"])
    fun loadEmojiText(textView: SimpleDraweeSpanTextView, text: String) {
        EmojiUtils.replaceFaceMsgByFresco(textView, text, 14)
    }

    @JvmStatic
    @BindingAdapter(value = ["recentContact"])
    fun loadEmojiTextWithAttachment(textView: SimpleDraweeSpanTextView, recentContact: RecentContact) {
        // 如果是自定义消息中的贴图消息
        if (recentContact.attachment != null && recentContact.attachment is StickerAttachment) {
            textView.text = "[贴图]"
            return
        }
        // 如果是自定义消息中的VR消息
        if (recentContact.attachment != null && recentContact.attachment is VRAttachment) {
            textView.text = "[VR带看]"
            return
        }
        // 如果是自定义消息中的楼盘卡片消息
        if (recentContact.attachment != null && recentContact.attachment is HouseAttachment) {
            val jsonObject = JSONObject((recentContact.attachment as HouseAttachment).houseJson)
            textView.text = "[${jsonObject.getString("houseTitle")}]"
            return
        }
        // 如果是自定义消息中的用户信息
        if (recentContact.attachment != null && recentContact.attachment is UserInfoAttachment) {
            val jsonObject = JSONObject((recentContact.attachment as UserInfoAttachment).userInfoJson)
            // 当前消息是由对方发送的，即B端用户收到的C端用户信息消息
            if (recentContact.contactId == recentContact.fromAccount) {
                textView.text = "${jsonObject.getString("userInfo")}"
            }
            else {
                textView.text = "${jsonObject.getString("lastMessages")}"
            }
            return
        }
        // 未知类型卡片
        if (recentContact.attachment != null && recentContact.attachment is DefaultCustomAttachment) {
            textView.text = "收到了一个不支持的消息，请升级淘房App之后查看"
            return
        }
        // 语音消息
        if (recentContact.msgType == MsgTypeEnum.audio) {
            textView.text = "[语音消息]"
            return
        }
        EmojiUtils.replaceFaceMsgByFresco(textView, recentContact.content, 14)
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
        textView.text = "${duration}s"
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
            textView.text = "已读"
        }
        else {
            textView.visibility = if (imMessage.status == MsgStatusEnum.success) {
                View.VISIBLE
            } else {
                View.GONE
            }
            textView.text = "未读"
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
            try {
                if ((imMessage.attachment as ImageAttachment).thumbUrl == null) {
                    ""
                }
                else {
                    (imMessage.attachment as ImageAttachment).thumbUrl
                }
            } catch (e: Exception) {
                ""
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
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(150f), SizeUtils.dp2px(150f))).build()
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
    @BindingAdapter(value = ["cvListVRStatueRelativeLayout"])
    fun loadChatListVRStatue(view: RelativeLayout, aVChatTypeEnum: AVChatTypeEnum) {
        when(aVChatTypeEnum) {
            AVChatTypeEnum.VALID -> {
                view.visibility = View.VISIBLE
            }
            AVChatTypeEnum.INVALID -> {
                view.visibility = View.GONE
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListVRStatueTextView"])
    fun loadChatListVRStatue(view: LinearLayout, aVChatTypeEnum: AVChatTypeEnum) {
        when(aVChatTypeEnum) {
            AVChatTypeEnum.VALID -> {
                view.visibility = View.GONE
            }
            AVChatTypeEnum.INVALID -> {
                view.visibility = View.VISIBLE
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListHouseCardImage"])
    fun loadChatListHouseCardImage(simpleDraweeView: SimpleDraweeView, imMessage: IMMessage) {
        var houseJson: String? = null
        if (imMessage.attachment is HouseAttachment) {
            val attachment = imMessage.attachment as HouseAttachment
            houseJson = attachment.houseJson
        }
        else if (imMessage.attachment is VRAttachment) {
            val attachment = imMessage.attachment as VRAttachment
            houseJson = attachment.vrJson
        }
        try {
            val jsonObject = JSONObject(houseJson)
            if (simpleDraweeView.tag !=null &&
                    !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                    simpleDraweeView.tag.toString() == jsonObject.getString("coverPic")) {
                // 什么都不做，防止Fresco闪烁
            }
            else {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(jsonObject.getString("coverPic")))
                        .setResizeOptions(ResizeOptions(SizeUtils.dp2px(86f), SizeUtils.dp2px(64f))).build()
                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request).setAutoPlayAnimations(true).build()
                simpleDraweeView.controller = draweeController
                simpleDraweeView.tag = jsonObject.getString("coverPic")
            }
        } catch (e: Exception) {

        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListHouseCardTitle"])
    fun loadChatListHouseCardTitle(textView: TextView, imMessage: IMMessage) {
        var houseJson: String? = null
        if (imMessage.attachment is HouseAttachment) {
            val attachment = imMessage.attachment as HouseAttachment
            houseJson = attachment.houseJson
        }
        else if (imMessage.attachment is VRAttachment) {
            val attachment = imMessage.attachment as VRAttachment
            houseJson = attachment.vrJson
        }
        try {
            val jsonObject = JSONObject(houseJson)
            textView.text = jsonObject.getString("houseTitle")
        } catch (e: Exception) {

        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListHouseCardPrice"])
    fun loadChatListHouseCardPrice(textView: TextView, imMessage: IMMessage) {
        var houseJson: String? = null
        if (imMessage.attachment is HouseAttachment) {
            val attachment = imMessage.attachment as HouseAttachment
            houseJson = attachment.houseJson
        }
        else if (imMessage.attachment is VRAttachment) {
            val attachment = imMessage.attachment as VRAttachment
            houseJson = attachment.vrJson
        }
        try {
            val jsonObject = JSONObject(houseJson)
            textView.text = jsonObject.getString("price")
        } catch (e: Exception) {

        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListHouseCardContent"])
    fun loadChatListHouseCardContent(textView: TextView, imMessage: IMMessage) {
        if (imMessage.attachment is HouseAttachment) {
            val attachment = imMessage.attachment as HouseAttachment
            val houseJson = attachment.houseJson
            try {
                val jsonObject = JSONObject(houseJson)
                val type = jsonObject.getInt("type")
                when(type) {
                    1 -> textView.text = "${jsonObject.getString("dist")} ${jsonObject.getString("channel")}"
                    2 -> textView.text = "${jsonObject.getString("apartment")} ${jsonObject.getString("buildarea")}"
                    3 -> textView.text = "${jsonObject.getString("apartment")} ${jsonObject.getString("buildarea")}"
                }
            } catch (e: Exception) {

            }
        }
        else if (imMessage.attachment is VRAttachment) {
            val attachment = imMessage.attachment as VRAttachment
            val houseJson = attachment.vrJson
            try {
                val jsonObject = JSONObject(houseJson)
                val type = jsonObject.getInt("type")
                when(type) {
                    1 -> textView.text = "${jsonObject.getString("apartment")} ${jsonObject.getString("desp")} ${jsonObject.getString("buildarea")} ${jsonObject.getString("unitType")}"
                    2 -> textView.text = "${jsonObject.getString("apartment")} ${jsonObject.getString("desp")} ${jsonObject.getString("buildarea")}"
                    3 -> textView.text = "${jsonObject.getString("apartment")} ${jsonObject.getString("desp")} ${jsonObject.getString("buildarea")}"
                }
            } catch (e: Exception) {

            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListUserInfoTitle"])
    fun loadChatListUserInfoTitle(textView: TextView, imMessage: IMMessage) {
        val attachment = imMessage.attachment as UserInfoAttachment
        val userInfoJson = attachment.userInfoJson
        try {
            val jsonObject = JSONObject(userInfoJson)
            textView.text = jsonObject.getString("userInfo")
        } catch (e: Exception) {

        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListLocationTitle"])
    fun loadChatListLocationTitle(textView: TextView, imMessage: IMMessage) {
        val attachment = imMessage.attachment as LocationAttachment
        textView.text = attachment.address
    }

    @JvmStatic
    @BindingAdapter(value = ["contactStar"])
    fun loadcontactStar(imageView: ImageView, nimUserInfo: NimUserInfo) {
        if (FriendManager.getFriendByAccount(nimUserInfo.account).extension != null &&
                FriendManager.getFriendByAccount(nimUserInfo.account).extension.containsKey(CommonParams.STAR) &&
                FriendManager.getFriendByAccount(nimUserInfo.account).extension[CommonParams.STAR] == "1") {
            imageView.setImageResource(R.mipmap.ic_contact_star_press)
        }
        else {
            imageView.setImageResource(R.mipmap.ic_contact_star_normal)
        }
    }
}