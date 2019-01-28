package com.readystatesoftware.chuck.internal.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.readystatesoftware.chuck.R

class SettingsActivity : BaseChuckActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chuck_activity_settings)
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}