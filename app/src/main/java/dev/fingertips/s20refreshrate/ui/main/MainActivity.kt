package dev.fingertips.s20refreshrate.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.fingertips.s20refreshrate.*
import dev.fingertips.s20refreshrate.ui.apps.AppsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RefreshApplication.appComponent.inject(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, AppsFragment())
            .commit()
    }
}