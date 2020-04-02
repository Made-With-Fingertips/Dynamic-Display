package dev.fingertips.s20refreshrate

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.MutableLiveData
import d
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
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
            // TODO: A list of system package names to ignore
            event.packageName.toString().let { packageName ->
                d { packageName }
                if (packageName == lastPackageName) return

                launch {
                    val app = appDao.getApp(packageName)
                    if (app != null) {
                        when (app.mode) {
                            Mode.SIXTY -> refreshRate.set60Hz()
                            Mode.ONE_TWENTY -> refreshRate.set120Hz()
                            else -> refreshRate.setDefault()
                        }
                    } else {
                        refreshRate.setDefault()
                    }

                    lastPackageName = packageName
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serviceConnected.postValue(false)
        job.cancel()
        return super.onUnbind(intent)
    }

    companion object {
        val serviceConnected = MutableLiveData<Boolean>()

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