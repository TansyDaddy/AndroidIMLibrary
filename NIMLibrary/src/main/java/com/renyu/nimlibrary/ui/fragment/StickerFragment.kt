package com.renyu.nimlibrary.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.ui.adapter.StickerAdapter
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.util.sticker.StickerCategory
import com.renyu.nimlibrary.util.sticker.StickerItem
import kotlinx.android.synthetic.main.fragment_emoji.*

class StickerFragment : Fragment() {

    companion object {
        fun getInstance(stickerCategory: StickerCategory): StickerFragment {
            val fragment = StickerFragment()
            val bundle = Bundle()
            bundle.putSerializable("sticker", stickerCategory)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val faceAdapter = StickerAdapter(arguments!!.getSerializable("sticker") as StickerCategory, object : StickerAdapter.OnItemClickListener {
            override fun onItemClick(stickerItem: StickerItem) {
                RxBus.getDefault().post(ObserveResponse(stickerItem, ObserveResponseType.Sticker))
            }
        })

        rv_emoji.setHasFixedSize(true)
        rv_emoji.layoutManager = GridLayoutManager(context, 4)
        rv_emoji.adapter = faceAdapter
    }
}