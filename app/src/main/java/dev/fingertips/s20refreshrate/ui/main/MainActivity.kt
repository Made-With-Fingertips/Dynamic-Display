package dev.fingertips.s20refreshrate.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_default) {
            showDefaultDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
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

    private fun showDefaultDialog() {
        val selected = when (preferences.defaultRate) {
            60 -> 0
            120 -> 1
            else -> -1
        }

        MaterialDialog(this).show {
            title = getString(R.string.action_default)
            listItemsSingleChoice(items = listOf(getString(R.string.sixty_hz), getString(R.string.one_twenty_hz)), initialSelection = selected) { _, index, _ ->
                when (index) {
                    0 -> preferences.defaultRate = 60
                    1 -> preferences.defaultRate = 120
                }
            }
        }
    }
}