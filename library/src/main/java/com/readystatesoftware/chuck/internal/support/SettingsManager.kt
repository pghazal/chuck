package com.readystatesoftware.chuck.internal.support

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(private val context: Context?) {

    companion object {
        private const val PREFS_NAME = "chuck_settings_preferences"

        private const val KEY_ERROR_400 = "chuck_settings_error_400"
        private const val KEY_ERROR_500 = "chuck_settings_error_500"
        private const val KEY_ERROR_MALFORMED_JSON = "chuck_settings_error_malformed_json"
    }

    private val sharedPref: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, 0)


    fun isError400FilterEnabled(): Boolean {
        if (sharedPref == null) {
            throw RuntimeException("Shared Prefs are not initialized")
        }

        return sharedPref.getBoolean(KEY_ERROR_400, false)
    }

    fun isError500FilterEnabled(): Boolean {
        if (sharedPref == null) {
            throw RuntimeException("Shared Prefs are not initialized")
        }

        return sharedPref.getBoolean(KEY_ERROR_500, false)
    }

    fun isErrorMalformedJsonFilterEnabled(): Boolean {
        if (sharedPref == null) {
            throw RuntimeException("Shared Prefs are not initialized")
        }

        return sharedPref.getBoolean(KEY_ERROR_MALFORMED_JSON, false)
    }

    fun setError400FilterEnabled(enable: Boolean) {
        sharedPref?.edit()?.putBoolean(KEY_ERROR_400, enable)?.apply()
    }

    fun setError500FilterEnabled(enable: Boolean) {
        sharedPref?.edit()?.putBoolean(KEY_ERROR_500, enable)?.apply()
    }

    fun setErrorMalformedJsonFilterEnabled(enable: Boolean) {
        sharedPref?.edit()?.putBoolean(KEY_ERROR_MALFORMED_JSON, enable)?.apply()
    }
}