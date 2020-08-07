package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import dev.fingertips.s20refreshrate.Preferences
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.RefreshApplication
import dev.fingertips.s20refreshrate.db.App
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
import kotlinx.android.synthetic.main.fragment_apps.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AppsFragment : Fragment() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var packageManager: PackageManager
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var recyclerAdapter: AppListAdapter

    private var appStatusJob: Job? = null
    private var installedApps: List<PackageInfo>? = null
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        recyclerAdapter.setOnLongClickListener { packageName ->
            MaterialDialog(requireContext()).show {
                title(R.string.reset)
                negativeButton(android.R.string.cancel)
                positiveButton(android.R.string.ok) {
                        lifecycleScope.launch {
                            appDao.addAll(App(packageName, Mode.DEFAULT))
                        }
                    }
            }
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
            if (firstLoad) progress_bar.visibility = View.VISIBLE

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

                    firstLoad = false
                    progress_bar.visibility = View.GONE
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
        recyclerAdapter.removeOnLongClickListener()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        menu.findItem(R.id.action_search).setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.isIconified = false
                searchView.requestFocusFromTouch()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView.setQuery("", true)
                return true
            }

        })
        searchView.setOnCloseListener {
            recyclerAdapter.endSearch()
            true
        }
        searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) view.showKeyboard()
            else view.hideKeyboard()
        }
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                recyclerAdapter.search(query ?: "")
                recycler_view.scrollToPosition(0)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_default) {
            showDefaultDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getInstalledApps(): List<PackageInfo> {
        return requireContext().packageManager.getInstalledPackages(0)
    }

    private fun showDefaultDialog() {
        val selected = when (preferences.defaultRate) {
            60 -> 0
            120 -> 1
            else -> -1
        }

        MaterialDialog(requireContext()).show {
            title(R.string.action_default)
            listItemsSingleChoice(items = listOf(getString(R.string.sixty_hz), getString(R.string.one_twenty_hz)), initialSelection = selected) { _, index, _ ->
                when (index) {
                    0 -> preferences.defaultRate = 60
                    1 -> preferences.defaultRate = 120
                }
            }
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}