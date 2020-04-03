package dev.fingertips.s20refreshrate

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import d
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshRate @Inject constructor(
    private val contentResolver: ContentResolver,
    private val preferences: Preferences
) {
    var minRefreshRate: Float
        get() {
            return try {
                Settings.Secure.getFloat(contentResolver, MIN_REFRESH_RATE)
            } catch (e: RuntimeException) {
                60F
            }
        }
        set(value) {
            Settings.Secure.putFloat(contentResolver, MIN_REFRESH_RATE, value)
        }

    var peakRefreshRate: Float
        get()  {
            return try {
                Settings.Secure.getFloat(contentResolver, PEAK_REFRESH_RATE)
            } catch (e: RuntimeException) {
                60F
            }
        }
        set(value) {
            Settings.Secure.putFloat(contentResolver, PEAK_REFRESH_RATE, value)
        }

    var refreshRateMode: Int
        get() {
            return try {
                Settings.Secure.getInt(contentResolver, REFRESH_RATE_MODE)
            } catch (e: RuntimeException) {
                0
            }
        }
        set(value) {
            Settings.Secure.putInt(contentResolver, REFRESH_RATE_MODE, value)
        }

    private var lastRefreshRate: Float = 0F

    fun set60Hz() {
        if (lastRefreshRate != 60F) {
            d { "Changing to 60Hz" }
            minRefreshRate = 60.0F
            peakRefreshRate = 60.0F
            refreshRateMode = 0

            lastRefreshRate = 60F
        }
    }

    fun set120Hz() {
        if (lastRefreshRate != 120F) {
            d { "Changing to 120Hz" }
            minRefreshRate = 120.0F
            peakRefreshRate = 120.0F
            refreshRateMode = 2

            lastRefreshRate = 120F
        }
    }

    fun setDefault() {
        d { "Changing to default Hz" }
        when (preferences.defaultRate) {
            60 -> set60Hz()
            120 -> set120Hz()
        }
    }

    companion object {
        const val MIN_REFRESH_RATE = "min_refresh_rate"
        const val PEAK_REFRESH_RATE = "peak_refresh_rate"
        const val REFRESH_RATE_MODE = "refresh_rate_mode"

        fun isWriteSecureSettingsGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        }
    }
}