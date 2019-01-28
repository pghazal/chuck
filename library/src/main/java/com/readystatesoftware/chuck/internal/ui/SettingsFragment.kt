package com.readystatesoftware.chuck.internal.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.internal.support.SettingsManager
import kotlinx.android.synthetic.main.chuck_fragment_settings.view.*

class SettingsFragment : Fragment() {

    private var filterByError400 = false
    private var filterByError500 = false
    private var filterByErrorMalformedJson = false

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        context?.let {
            settingsManager = SettingsManager(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.chuck_fragment_settings, container, false)

        filterByError400 = settingsManager.isError400FilterEnabled()
        filterByError500 = settingsManager.isError500FilterEnabled()
        filterByErrorMalformedJson = settingsManager.isErrorMalformedJsonFilterEnabled()

        view.error400Switch.isChecked = filterByError400
        view.error500Switch.isChecked = filterByError500
        view.errorMalformedJsonSwitch.isChecked = filterByErrorMalformedJson

        view.error400Switch.setOnCheckedChangeListener { _, checked ->
            settingsManager.setError400FilterEnabled(checked)
        }

        view.error500Switch.setOnCheckedChangeListener { _, checked ->
            settingsManager.setError500FilterEnabled(checked)
        }

        view.errorMalformedJsonSwitch.setOnCheckedChangeListener { _, checked ->
            settingsManager.setErrorMalformedJsonFilterEnabled(checked)
        }

        return view
    }
}