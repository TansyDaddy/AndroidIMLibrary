package com.renyu.nimlibrary.manager

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService

object FriendManager {
    fun addToBlackList(account: String) {
        NIMClient.getService(FriendService::class.java)
                .addToBlackList(account)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    fun removeFromBlackList(account: String) {
        NIMClient.getService(FriendService::class.java)
                .removeFromBlackList(account)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {

                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }
}