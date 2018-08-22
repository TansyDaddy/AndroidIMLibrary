package com.renyu.nimlibrary.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ZoomControls
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.SizeUtils
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.util.OtherUtils
import kotlinx.android.synthetic.main.activity_location.*


class MapPreviewActivity : AppCompatActivity() {

    // 地图初始化
    private val mBaiduMap: BaiduMap by lazy {
        map_location.map
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // 隐藏原生的控制按钮
        map_location.showZoomControls(false)
        // 隐藏比例尺
        map_location.showScaleControl(false)
        val child = map_location.getChildAt(1)
        // 隐藏logo
        if (child != null && (child is ImageView || child is ZoomControls)) {
            child.visibility = View.INVISIBLE
        }
        mBaiduMap.setOnMapLoadedCallback {
            val ll = LatLng(intent.getDoubleExtra("lat", 0.toDouble()), intent.getDoubleExtra("lng", 0.toDouble()))
            val builder = MapStatus.Builder()
            builder.target(ll).zoom(15.0f)
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))

            initOverlay(intent.getStringExtra("address"),
                    intent.getDoubleExtra("lat", 0.toDouble()),
                    intent.getDoubleExtra("lng", 0.toDouble()))
        }
    }

    private fun initOverlay(address: String, lat: Double, lng: Double) {
        val view = LayoutInflater.from(this).inflate(R.layout.view_mapoverlay, null, false)
        val tvAddress = view.findViewById(R.id.tv_address) as TextView
        tvAddress.text = address
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bd = OtherUtils.getBitmapDescriptor(view)
        val bundle = Bundle()
        bundle.putParcelable("LatLng", LatLng(lat, lng))
        bundle.putString("address", address)
        val oo = MarkerOptions().position(LatLng(lat, lng)).yOffset(-SizeUtils.dp2px(50f)).extraInfo(bundle).icon(bd).zIndex(0)
        oo.animateType(MarkerOptions.MarkerAnimateType.grow)
        mBaiduMap.addOverlay(oo) as Marker
    }

    override fun onPause() {
        map_location.onPause()
        super.onPause()
    }

    override fun onResume() {
        map_location.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        // 关闭定位图层
        mBaiduMap.isMyLocationEnabled = false
        map_location.onDestroy()
        super.onDestroy()
    }
}