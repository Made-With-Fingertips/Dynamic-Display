package dev.fingertips.s20refreshrate

import android.content.Context
import androidx.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    context: Context
) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var defaultRate: Int
        get() = prefs.getInt("defaultRate", 0)
        set(value) = prefs.edit().putInt("defaultRate", value).apply()

    var skipCompatibilityCheck: Boolean
        get() = prefs.getBoolean("skipCompatibilityCheck", false)
        set(value) = prefs.edit().putBoolean("skipCompatibilityCheck", value).apply()

    var latestVersion: String?
        get() = prefs.getString("latestVersion", null)
        set(value) = prefs.edit().putString("latestVersion", value).apply()

    var hideSystemApps: Boolean
        get() = prefs.getBoolean("hideSystemApps", false)
        set(value) = prefs.edit().putBoolean("hideSystemApps", value).apply()

    var firstRun: Boolean
        get() = prefs.getBoolean("firstRun", true)
        set(value) = prefs.edit().putBoolean("firstRun", value).apply()
}