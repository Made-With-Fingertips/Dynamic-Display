package dev.fingertips.s20refreshrate

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import d
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
import dev.fingertips.s20refreshrate.ui.permissions.PermissionsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class RefreshService : AccessibilityService(), CoroutineScope {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var refreshRate: RefreshRate

    private val job = SupervisorJob()

    private var lastPackageName: String? = null

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    override fun onServiceConnected() {
        super.onServiceConnected()
        RefreshApplication.appComponent.inject(this)
        d { "RefreshService connected" }
        serviceConnected.postValue(true)
    }

    override fun onInterrupt() {
        job.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName.toString().let { packageName ->
                d { packageName }
                if (packageName == lastPackageName) return
                if (ignoredPackages.contains(packageName)) return

                launch {
                    val app = appDao.getApp(packageName)
                    try {
                        if (app != null) {
                            when (app.mode) {
                                Mode.SIXTY -> refreshRate.set60Hz(packageName)
                                Mode.NINETY_SIX -> refreshRate.set96Hz(packageName)
                                Mode.ONE_TWENTY -> refreshRate.set120Hz(packageName)
                                else -> refreshRate.setDefault(packageName)
                            }
                        } else {
                            refreshRate.setDefault(packageName)
                        }

                        lastPackageName = packageName
                        try {
                            val appInfo = packageManager.getApplicationInfo(packageName, 0)
                            refreshRate.lastRunningAppName = appInfo.loadLabel(packageManager).toString()
                        } catch (e: PackageManager.NameNotFoundException) {
                            refreshRate.lastRunningAppName = getString(R.string.app_name)
                        }
                    } catch (e: SecurityException) {
                        notifyPermissionNeeded()
                    }
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serviceConnected.postValue(false)
        job.cancel()
        return super.onUnbind(intent)
    }

    private fun notifyPermissionNeeded() {
        val intent = Intent(Intent.ACTION_VIEW, PermissionsFragment.INSTRUCTIONS_URI)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, "adb").apply {
            setSmallIcon(R.drawable.ic_stat_adb)
            setContentTitle(getString(R.string.permission_adb_missing))
            setContentText(getString(R.string.permission_adb_missing_sub))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_LOW
        }

        with(NotificationManagerCompat.from(this)) {
            notify(7, builder.build())
        }
    }

    companion object {
        val serviceConnected = MutableLiveData<Boolean>()

        val ignoredPackages = listOf(
            "com.android.systemui", // Notification shade and quick settings
            "com.samsung.android.app.cocktailbarservice" // Edge Screen
        )

        fun isAccessibilityServiceEnabled(context: Context, packageName: String): Boolean {
            val am = context.getSystemService(AccessibilityManager::class.java) ?: throw IllegalStateException("Unable to get AccessibilityManager")
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            enabledServices.forEach {
                it.resolveInfo.serviceInfo.let { info ->
                    if (info.packageName == packageName && info.name == RefreshService::class.java.name) return true
                }
            }

            return false
        }
    }
}