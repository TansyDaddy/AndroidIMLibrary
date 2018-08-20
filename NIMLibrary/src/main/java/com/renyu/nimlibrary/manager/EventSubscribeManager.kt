package com.renyu.nimlibrary.manager

import android.util.Log
import com.blankj.utilcode.util.NetworkUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.event.EventSubscribeService
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver
import com.netease.nimlib.sdk.event.model.Event
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent
import org.json.JSONException
import org.json.JSONObject

object EventSubscribeManager {

    // 多端在线状态配置解析
    val KEY_NET_STATE = "net_state"
    // 0在线  1忙碌  2离开"
    val KEY_ONLINE_STATE = "online_state"
    // 发布事件有效期7天
    val EVENT_EXPIRY = (60 * 60 * 24 * 7).toLong()
    // 订阅有效期 1天，单位秒
    val SUBSCRIBE_EXPIRY = (60 * 60 * 24).toLong()

    // 当前在线的设备
    val onlineAccent = HashSet<String>()

    /**
     * 订阅指定账号的在线状态事件
     */
    fun subscribeEvent(accounts: List<String>) {
        val eventSubscribeRequest = EventSubscribeRequest()
        eventSubscribeRequest.eventType = NimOnlineStateEvent.EVENT_TYPE
        eventSubscribeRequest.publishers = accounts
        eventSubscribeRequest.expiry = SUBSCRIBE_EXPIRY
        eventSubscribeRequest.isSyncCurrentValue = true
        NIMClient.getService(EventSubscribeService::class.java).subscribeEvent(eventSubscribeRequest)
                .setCallback(object : RequestCallbackWrapper<List<String>>() {
                    override fun onResult(code: Int, result: List<String>?, exception: Throwable?) {
                        if (code == ResponseCode.RES_SUCCESS.toInt()) {
                            if (result != null) {

                            }
                        }
                    }
                })
        NIMClient.getService(EventSubscribeServiceObserver::class.java).observeEventChanged({ t ->
            t?.forEach {
                if (NimOnlineStateEvent.isOnlineStateEvent(it)) {
                    if (JSONObject(it.nimConfig).getJSONArray("online").length() == 0) {
                        onlineAccent.add(it.publisherAccount)
                        Log.d("NIM_APP", "${it.publisherAccount}下线")
                    } else {
                        onlineAccent.remove(it.publisherAccount)
                        Log.d("NIM_APP", "${it.publisherAccount}上线")
                    }
                }
            }
        }, true)
    }

    /**
     * 发布自己在线状态
     */
    fun publishEvent() {
        val netState = when(NetworkUtils.getNetworkType()) {
            NetworkUtils.NetworkType.NETWORK_ETHERNET -> { 2 }
            NetworkUtils.NetworkType.NETWORK_WIFI -> { 1 }
            NetworkUtils.NetworkType.NETWORK_4G -> { 5 }
            NetworkUtils.NetworkType.NETWORK_3G -> { 4 }
            NetworkUtils.NetworkType.NETWORK_2G -> { 3 }
            NetworkUtils.NetworkType.NETWORK_UNKNOWN -> { 0 }
            NetworkUtils.NetworkType.NETWORK_NO -> { -1 }
        }
        if (netState == -1) {
            return
        }
        val event = buildOnlineStateEvent(netState, 0, true, false, EVENT_EXPIRY)
        NIMClient.getService(EventSubscribeService::class.java).publishEvent(event)
    }

    /**
     * 构建一个在线状态事件
     *
     * @param netState 当前在线网络状态
     * @param syncSelfEnable 是否多端同步
     * @param broadcastOnlineOnly 是否只广播给在线用户
     * @param expiry 事件有效期，单位秒
     * @return event
     */
    private fun buildOnlineStateEvent(netState: Int, onlineState: Int, syncSelfEnable: Boolean, broadcastOnlineOnly: Boolean, expiry: Long): Event {
        val event = Event(NimOnlineStateEvent.EVENT_TYPE, NimOnlineStateEvent.MODIFY_EVENT_CONFIG, expiry)
        event.isSyncSelfEnable = syncSelfEnable
        event.isBroadcastOnlineOnly = broadcastOnlineOnly
        event.config = buildConfig(netState, onlineState)
        return event
    }

    private fun buildConfig(netState: Int, onlineState: Int): String {
        val json = JSONObject()
        try {
            json.put(KEY_NET_STATE, netState)
            json.put(KEY_ONLINE_STATE, onlineState)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json.toString()
    }
}