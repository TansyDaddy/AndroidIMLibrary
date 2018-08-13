package com.renyu.nimapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import java.io.File

class TakePhotoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            val intent = Intent()
            intent.putExtra("fileName", Environment.getExternalStorageDirectory().path + File.separator + "1.jpg")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }, 3000)
    }
}