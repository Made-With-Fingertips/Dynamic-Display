package dev.fingertips.s20refreshrate.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.fingertips.s20refreshrate.BuildConfig
import dev.fingertips.s20refreshrate.Preferences
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.RefreshApplication
import dev.fingertips.s20refreshrate.net.UpdateChecker
import kotlinx.android.synthetic.main.activity_about.*
import javax.inject.Inject

class AboutActivity : AppCompatActivity() {
    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        app_version.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)
        RefreshApplication.appComponent.inject(this)

        github_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, GITHUB_URI)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        preferences.latestVersion?.let {
            val latestVersion = UpdateChecker.LatestVersion.fromPreferenceString(it)
            if (latestVersion.code > BuildConfig.VERSION_CODE) {
                update_version.text = getString(R.string.about_update_version, latestVersion.version)
                update_group.visibility = View.VISIBLE
                update_download_button.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, GITHUB_RELEASES_URI)
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val GITHUB_URL = "https://github.com/Made-With-Fingertips/S20-Refresh-Rate/"
        private const val GITHUB_RELEASES_URL = "https://github.com/Made-With-Fingertips/S20-Refresh-Rate/releases"

        private val GITHUB_URI = Uri.parse(GITHUB_URL)
        private val GITHUB_RELEASES_URI = Uri.parse(GITHUB_RELEASES_URL)
    }
}