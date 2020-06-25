package dev.fingertips.s20refreshrate.ui.apps

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.RefreshApplication
import dev.fingertips.s20refreshrate.db.App
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
import kotlinx.android.synthetic.main.fragment_apps.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AppsFragment : Fragment() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var packageManager: PackageManager
    @Inject lateinit var recyclerAdapter: AppListAdapter

    private var appStatusJob: Job? = null
    private var installedApps: List<PackageInfo>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        RefreshApplication.appComponent.inject(this)

        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.setHasFixedSize(false)
        recycler_view.adapter = recyclerAdapter

        recyclerAdapter.setOnClickListener {
            AppDetailFragment.newInstance(it).show(requireFragmentManager(), it)
        }

        fast_scroll.setupWithRecyclerView(recycler_view, { position ->
            val item = recyclerAdapter.getItemTitle(position)
            FastScrollItemIndicator.Text(item.substring(0, 1).toUpperCase())
        })

        fast_scroll_thumb.setupWithFastScroller(fast_scroll)

        recyclerAdapter.onModeChangeListener = object: AppListAdapter.OnModeChangeListener {
            override fun onModeChange(packageName: String, newMode: Mode) {
                lifecycleScope.launch {
                    appDao.addAll(App(packageName, newMode))
                }
            }
        }

        appDao.getAllAppsAsLiveData().observe(this, Observer { apps ->
            // recyclerAdapter.updateSelectedAppsList(apps)
        })
    }

    override fun onResume() {
        super.onResume()

        appStatusJob = lifecycleScope.launch {
            installedApps = getInstalledApps()
            Timber.d("Got ${installedApps?.size} installed apps")

            appDao.getAllAppsAsFlow().collect { apps ->
                Timber.d("Got ${apps.size} apps in db")
                if (installedApps != null) {
                    val appItems = installedApps!!.map { info ->
                        AppListAdapter.AppItem(
                            name = info.applicationInfo.loadLabel(packageManager).toString(),
                            packageName = info.packageName,
                            icon = info.applicationInfo.loadIcon(packageManager),
                            mode = apps.find { it.packageName == info.packageName }?.mode ?: Mode.DEFAULT
                        )
                    }.sortedBy { it.name.toLowerCase(Locale.getDefault()) }

                    Timber.d("Created ${appItems.size} AppItems")

                    recyclerAdapter.updateAppsList(appItems)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appStatusJob?.cancel()
    }

    override fun onDestroy() {
        recyclerAdapter.removeOnClickListener()
        super.onDestroy()
    }

    private fun getInstalledApps(): List<PackageInfo> {
        return requireContext().packageManager.getInstalledPackages(0)
    }
}