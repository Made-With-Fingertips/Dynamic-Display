package dev.fingertips.s20refreshrate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dev.fingertips.s20refreshrate.di.AppComponent
import dev.fingertips.s20refreshrate.di.AppModule
import dev.fingertips.s20refreshrate.di.DaggerAppComponent
import dev.fingertips.s20refreshrate.di.DbModule
import timber.log.Timber
import javax.inject.Inject

class RefreshApplication : Application() {
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var refreshRate: RefreshRate

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(RefreshTree())

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .dbModule(DbModule())
            .build()

        appComponent.inject(this)
        preferences.defaultRate.let { value ->
            if (value == 0) {
                preferences.defaultRate = 60
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("adb", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        lateinit var appComponent: AppComponent
    }

    internal class RefreshTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, "RefreshRate: $tag", message, t)
        }
    }
}