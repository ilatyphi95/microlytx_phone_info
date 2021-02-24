package com.ilatyphi95.microlytxphoneinfo.ui.preferenceActivity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.ilatyphi95.microlytxphoneinfo.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val themePref = findPreference<ListPreference>(getString(R.string.theme_key))

            themePref?.setOnPreferenceChangeListener{_, newValue ->
                val auto = getString(R.string.key_auto)
                val light = getString(R.string.key_light_theme)
                val dark = getString(R.string.key_dark_theme)

                if( newValue is String) {
                    when(newValue) {
                        auto -> updateTheme(AppCompatDelegate.MODE_NIGHT_AUTO)
                        light -> updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
                        dark -> updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                true
            }
        }

        private fun updateTheme(nightMode: Int) : Boolean {
            AppCompatDelegate.setDefaultNightMode(nightMode)
            requireActivity().recreate()
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when(item.itemId) {
                R.id.home -> {
                    requireActivity().onBackPressed()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }
}