package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.renyu.nimapp.params.InitParams
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.util.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposable = RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.type == ObserveResponseType.Kickout) {
                        jumpToSignIn()
                    }
                }.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun jumpToSignIn() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(InitParams.TYPE, InitParams.KICKOUT)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}