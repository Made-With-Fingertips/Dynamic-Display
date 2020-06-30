package dev.fingertips.s20refreshrate.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import d
import dev.fingertips.s20refreshrate.*
import dev.fingertips.s20refreshrate.ui.apps.AppsFragment
import dev.fingertips.s20refreshrate.ui.permissions.PermissionsFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RefreshApplication.appComponent.inject(this)

        RefreshService.serviceConnected.observe(this, Observer {
            checkPermissions()
        })

        checkPermissions()
    }

    private fun checkPermissions() {
        val serviceConnected = RefreshService.isAccessibilityServiceEnabled(this, packageName)
        val writeGranted = RefreshRate.isWriteSecureSettingsGranted(this)

        if (serviceConnected && writeGranted) {
            RefreshService.serviceConnected.removeObservers(this)
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AppsFragment())
                .commit()
        } else {
            d { "Starting PermissionsFragment" }
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, PermissionsFragment.newInstance(serviceConnected, writeGranted))
                .commit()
        }
    }
}