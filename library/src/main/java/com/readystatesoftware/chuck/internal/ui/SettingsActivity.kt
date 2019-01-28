package com.readystatesoftware.chuck.internal.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.readystatesoftware.chuck.R

class SettingsActivity : BaseChuckActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chuck_activity_settings)
    }

    companion object {

        fun start(fragment: Fragment, requestCode: Int) {
            val intent = Intent(fragment.context, SettingsActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)
        }
    }
}