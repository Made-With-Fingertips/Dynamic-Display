package dev.fingertips.s20refreshrate

import android.app.Application
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