package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.fingertips.s20refreshrate.Preferences
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.RefreshApplication
import dev.fingertips.s20refreshrate.RefreshRate
import dev.fingertips.s20refreshrate.db.App
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
import kotlinx.android.synthetic.main.fragment_app_detail.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppDetailFragment : BottomSheetDialogFragment() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var packageManager: PackageManager
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var refreshRate: RefreshRate

    private var app: App? = null
    private lateinit var packageInfo: PackageInfo

    override fun onAttach(context: Context) {
        RefreshApplication.appComponent.inject(this)
        super.onAttach(context)
    }

    companion object {
        private const val ARG_PACKAGE_NAME = "packageName"

        fun newInstance(packageName: String): AppDetailFragment {
            val fragment = AppDetailFragment()
            val arguments = bundleOf(ARG_PACKAGE_NAME to packageName)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_PACKAGE_NAME, null).let { packageName ->
            if (packageName == null) {
                dismiss()
            } else {
                packageInfo = packageManager.getPackageInfo(packageName, 0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        app_icon.setImageDrawable(packageInfo.applicationInfo.loadIcon(packageManager))
        app_name.text = packageInfo.applicationInfo.loadLabel(packageManager)

        lifecycleScope.launch {
            appDao.getApp(packageInfo.packageName).let {
                app = it

                with (toggle_group) {
                    when (app?.mode) {
                        Mode.SIXTY -> this.check(R.id.toggle_60hz)
                        Mode.NINETY_SIX -> this.check(R.id.toggle_96hz)
                        Mode.ONE_TWENTY -> this.check(R.id.toggle_120hz)
                    }
                }
            }
        }

        // TODO: Notify user they HAVE to press save to actually save
        save_button.setOnClickListener {
            val newMode = when (toggle_group.checkedButtonId) {
                R.id.toggle_60hz -> Mode.SIXTY
                R.id.toggle_96hz -> Mode.NINETY_SIX
                R.id.toggle_120hz -> Mode.ONE_TWENTY
                else -> Mode.DEFAULT
            }

            lifecycleScope.launch {
                appDao.addAll(App(packageInfo.packageName, newMode))
                dismiss()
            }
        }
    }
}