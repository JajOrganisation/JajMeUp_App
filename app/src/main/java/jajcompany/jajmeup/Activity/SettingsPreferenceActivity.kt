package jajcompany.jajmeup.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import jajcompany.jajmeup.R


class SettingsPreferenceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferencesTheme)
        fragmentManager.beginTransaction().replace(android.R.id.content,
                PrefsFragment()).commit()
    }

    class PrefsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferencesettings)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
        override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
            when (key) {
                "default_reveil" -> {
                    findPreference("default_reveil").editor.putString("reveil_default", "").apply()
                }
            }
        }
    }


    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SettingsPreferenceActivity::class.java)
            return intent
        }
    }
}