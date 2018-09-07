package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.ClientType
import com.netease.nimlib.sdk.auth.OnlineClient
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.util.RxBus

object StatueManager {

    /**
     * 监听多端登录状态
     */
    @JvmStatic
    fun observeOtherClients() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeOtherClients(com.netease.nimlib.sdk.Observer<List<OnlineClient>> { t ->
                    if (t == null || t.isEmpty()) {
                        return@Observer
                    } else {
                        when(t[0].clientType) {
                            ClientType.Windows -> {

                            }
                            ClientType.MAC -> {

                            }
                            ClientType.Web -> {

                            }
                            ClientType.iOS -> {

                            }
                            ClientType.Android -> {

                            }
                            ClientType.UNKNOW -> {

                            }
                        }
                    }
                }, true)
    }

    /**
     * 监听用户在线状态
     */
    @JvmStatic
    fun observeOnlineStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeOnlineStatus({
                    // 踢下线
                    if (it.wontAutoLogin()) {
                        Log.d("NIM_APP", "被踢下线")

                        UserManager.setUserAccount(null, null)

                        CommonParams.isKickout = true

                        // 第三方框架自行处理踢下线方法
                        val clazz = Class.forName("com.nimapp.params.NimInitParams")
                        val kickoutFuncMethod = clazz.getDeclaredMethod("kickoutFunc")
                        kickoutFuncMethod.invoke(null)
                    }
                    else {
                        RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.OnlineStatus))

                        when (it) {
                            StatusCode.NET_BROKEN -> Log.d("NIM_APP", "在线状态：当前网络不可用")
                            StatusCode.UNLOGIN -> Log.d("NIM_APP", "在线状态：未登录")
                            StatusCode.CONNECTING -> Log.d("NIM_APP", "在线状态：连接中")
                            StatusCode.LOGINING -> Log.d("NIM_APP", "在线状态：登录中")
                            StatusCode.LOGINED -> {
                                Log.d("NIM_APP", "在线状态：登录成功")
                                // 发布自己的在线状态
                                EventSubscribeManager.publishEvent()
                            }
                            else -> {
                                Log.d("NIM_APP", "其他异常：$it")
                            }
                        }
                    }
                }, true)
    }

    /**
     * 监听数据同步状态
     */
    @JvmStatic
    fun observeLoginSyncDataStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeLoginSyncDataStatus({ t ->
                    Log.d("NIM_APP", "数据同步状态${t?.name}")
                    when(t) {
                        LoginSyncStatus.NO_BEGIN -> {

                        }
                        LoginSyncStatus.BEGIN_SYNC -> {

                        }
                        LoginSyncStatus.SYNC_COMPLETED -> {
                            // 消息同步完成
                            RxBus.getDefault().post(ObserveResponse(t, ObserveResponseType.ObserveLoginSyncDataStatus))
                        }
                    }
                }, true)
    }

}