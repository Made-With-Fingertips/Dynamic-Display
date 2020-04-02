package dev.fingertips.s20refreshrate.ui.apps

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
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppsFragment : Fragment() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var recyclerAdapter: AppListAdapter

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

        fast_scroll.setupWithRecyclerView(recycler_view, { position ->
            val item = recyclerAdapter.getItemTitle(position)
            FastScrollItemIndicator.Text(item.substring(0, 1).toUpperCase())
        })

        fast_scroll_thumb.setupWithFastScroller(fast_scroll)

        getInstalledApps()

        recyclerAdapter.onModeChangeListener = object: AppListAdapter.OnModeChangeListener {
            override fun onModeChange(packageName: String, newMode: Mode) {
                lifecycleScope.launch {
                    appDao.addAll(App(packageName, newMode))
                }
            }
        }

        appDao.getAllAppsAsLiveData().observe(this, Observer { apps ->
            recyclerAdapter.updateSelectedAppsList(apps)
        })
    }

    private fun getInstalledApps() {
        lifecycleScope.launch {
            val packages = requireContext().packageManager.getInstalledPackages(0)
            recyclerAdapter.updateAppsList(packages)
        }
    }
}