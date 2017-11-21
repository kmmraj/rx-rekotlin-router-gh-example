package org.rekotlinexample.github.apirequests

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Mohanraj Karatadipalayam on 11/10/17.
 */

object PreferenceApiService {
    val GITHUB_PREFS_NAME = "GITHUB_PREFS"
    val GITHUB_PREFS_KEY_USERNAME = "GITHUB_USERNAME"
    val GITHUB_PREFS_KEY_TOKEN = "GITHUB_PREFS_KEY_TOKEN"
    val GITHUB_PREFS_KEY_LOGINSTATUS = "GITHUB_LOGINSTATUS"

    fun savePreference(context: Context, key: String, value: String) {
        val settings: SharedPreferences = context.getSharedPreferences(GITHUB_PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = settings.edit()

        editor.putString(key, value)
        editor.commit()
    }

    fun getPreference(context: Context, key: String): String? {
        val settings = getSharedPreferenceByName(context,GITHUB_PREFS_NAME)
        return settings.getString(key, null)
    }

    fun getSharedPreferenceByName(context: Context, sharedPreferenceKey: String): SharedPreferences {
        return context.getSharedPreferences(sharedPreferenceKey, Context.MODE_PRIVATE)
    }

}