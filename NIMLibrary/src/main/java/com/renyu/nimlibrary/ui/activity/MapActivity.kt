package com.renyu.nimlibrary.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ZoomControls
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.SizeUtils
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.util.OtherUtils
import kotlinx.android.synthetic.main.activity_location.*


class MapActivity : AppCompatActivity() {

    // 地图初始化
    private val mBaiduMap: BaiduMap by lazy {
        map_location.map
    }

    // 定位初始化
    private val mLocClient: LocationClient by lazy {
        LocationClient(this)
    }

    var isFirstLoc = true // 是否首次定位

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // 开启定位图层
        mBaiduMap.isMyLocationEnabled = true
        mBaiduMap.setOnMarkerClickListener {
            val temp = Intent()
            temp.putExtra("LatLng", it.extraInfo.getParcelable<LatLng>("LatLng"))
            temp.putExtra("address", it.extraInfo.getString("address"))
            setResult(Activity.RESULT_OK, temp)
            finish()

            false
        }
        // 隐藏原生的控制按钮
        map_location.showZoomControls(false)
        // 隐藏比例尺
        map_location.showScaleControl(false)
        val child = map_location.getChildAt(1)
        // 隐藏logo
        if (child != null && (child is ImageView || child is ZoomControls)) {
            child.visibility = View.INVISIBLE
        }
        mLocClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(p0: BDLocation?) {
                // map view 销毁后不在处理新接收的位置
                if (p0 == null || map_location == null) {
                    return
                }
                val locData = MyLocationData.Builder()
                        .accuracy(p0.radius)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(0f).latitude(p0.latitude)
                        .longitude(p0.longitude).build()
                mBaiduMap.setMyLocationData(locData)
                if (isFirstLoc) {
                    isFirstLoc = false
                    val ll = LatLng(p0.latitude, p0.longitude)
                    val builder = MapStatus.Builder()
                    builder.target(ll).zoom(15.0f)
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
                    // 加载覆盖物
                    initOverlay(p0.addrStr, p0.latitude, p0.longitude)
                }
            }
        })
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(60*1000)
        option.setIsNeedAddress(true)
        mLocClient.locOption = option
        mLocClient.start()
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
        // 退出时销毁定位
        mLocClient.stop()
        // 关闭定位图层
        mBaiduMap.isMyLocationEnabled = false
        map_location.onDestroy()
        super.onDestroy()
    }
}