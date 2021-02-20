package com.ilatyphi95.microlytxphoneinfo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.ilatyphi95.microlytxphoneinfo.utils.NightMode
import java.util.*

class PhoneInfoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pref.getString(
            getString(R.string.theme_key),
            getString(R.string.key_auto)
        )?.apply {
             val mode = NightMode.valueOf(this.toUpperCase(Locale.US))
            AppCompatDelegate.setDefaultNightMode(mode.value)
        }
    }
}