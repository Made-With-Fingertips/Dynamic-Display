package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.os.Build
import dev.fingertips.s20refreshrate.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompatibilityCheck @Inject constructor(
    private val context: Context
){
    data class CompatibilityResult(
        val isCompatible: Boolean,
        val deviceModel: String
    )

    fun isDeviceCompatible(): CompatibilityResult {
        val model = Build.MODEL
        val compatibleDevices = context.resources.getStringArray(R.array.valid_devices_model_numbers)
        return CompatibilityResult(
            isCompatible = compatibleDevices.any { model.contains(it) },
            deviceModel = model
        )
    }
}