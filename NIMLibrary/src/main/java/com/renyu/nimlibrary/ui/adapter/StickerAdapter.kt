package com.renyu.nimlibrary.ui.adapter

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.util.sticker.StickerCategory
import com.renyu.nimlibrary.bean.StickerItem
import com.renyu.nimlibrary.util.sticker.StickerUtils
import kotlinx.android.synthetic.main.adapter_sticker.view.*

class StickerAdapter(private val stickerCategory: StickerCategory, val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<StickerAdapter.StickerHolder>() {

    interface OnItemClickListener {
        fun onItemClick(stickerItem: StickerItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_sticker, parent, false)
        return StickerHolder(view)
    }

    override fun getItemCount() = stickerCategory.stickers.size

    override fun onBindViewHolder(holder: StickerHolder, position: Int) {
        holder.showImage(position)
    }

    inner class StickerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showImage(position: Int) {
            if (itemView.iv_sticker_item.tag !=null &&
                    !TextUtils.isEmpty(itemView.iv_sticker_item.tag.toString()) &&
                    itemView.iv_sticker_item.tag.toString() == stickerCategory.stickers[position].name) {
                // 什么都不做，防止Fresco闪烁
            }
            else {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(StickerUtils.getStickerUri(stickerCategory.stickers[position].category, stickerCategory.stickers[position].name)))
                        .setResizeOptions(ResizeOptions(SizeUtils.dp2px(80f), SizeUtils.dp2px(80f))).build()
                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request).setAutoPlayAnimations(true).build()
                itemView.iv_sticker_item.controller = draweeController
                itemView.iv_sticker_item.tag = stickerCategory.stickers[position].name
            }
            itemView.iv_sticker_item.setOnClickListener {
                onItemClickListener.onItemClick(stickerCategory.stickers[position])
            }
        }
    }
}