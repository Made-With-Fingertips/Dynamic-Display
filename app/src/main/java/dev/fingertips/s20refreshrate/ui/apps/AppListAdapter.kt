package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.fingertips.s20refreshrate.Preferences
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.db.Mode
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AppListAdapter @Inject constructor(
    private val context: Context,
    private val preferences: Preferences
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {
    private val appsList = mutableListOf<AppItem>()
    private val originalAppsList = mutableListOf<AppItem>()

    private var onClickListener: (packageName: String) -> Unit = {}
    private var onLongClickListener: (packageName: String) -> Unit = {}

    var onModeChangeListener: OnModeChangeListener? = null

    var hideSystemApps = preferences.hideSystemApps
        set(value) {
            preferences.hideSystemApps = value
            field = value
        }

    override fun getItemCount(): Int = appsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        appsList[position].let { appItem ->
            holder.appName.text = appItem.name
            holder.icon.setImageDrawable(appItem.icon)

            holder.mode.text = when (appItem.mode) {
                Mode.SIXTY -> context.getString(R.string.sixty_hz)
                Mode.NINETY_SIX -> context.getString(R.string.ninety_six_hz)
                Mode.ONE_TWENTY -> context.getString(R.string.one_twenty_hz)
                else -> context.getString(R.string.default_hz)
            }

            holder.layout.setOnClickListener {
                onClickListener.invoke(appItem.packageName)
            }

            holder.layout.setOnLongClickListener {
                onLongClickListener.invoke(appItem.packageName)
                true
            }
        }
    }

    fun getItemTitle(position: Int): String {
        return appsList[position].name
    }

    fun setOnClickListener(listener: (packageName: String) -> Unit) {
        onClickListener = listener
    }

    fun removeOnClickListener() {
        onClickListener = {}
    }

    fun setOnLongClickListener(listener: (packageName: String) -> Unit) {
        onLongClickListener = listener
    }

    fun removeOnLongClickListener() {
        onLongClickListener = {}
    }

    private fun String.containsIgnoreCase(rhs: String): Boolean {
        return this.toLowerCase(Locale.getDefault()).contains(rhs.toLowerCase(Locale.getDefault()))
    }

    fun search(query: String) {
        val results = originalAppsList
            .filter { it.name.containsIgnoreCase(query) || it.packageName.containsIgnoreCase(query) }
            .sortedBy { it.name.toLowerCase(Locale.getDefault()) }
        updateAppsList(results, forceSystem = true)
    }

    fun endSearch() {
        updateAppsList(originalAppsList)
    }

    fun refreshList() {
        updateAppsList(originalAppsList)
    }

    fun updateAppsList(newList: List<AppItem>, forceSystem: Boolean = false) {
        Timber.d("Received ${newList.size} new AppItems")
        val filteredNewList = newList.filter {
            when {
                forceSystem -> true
                hideSystemApps -> !it.isSystem
                else -> true
            }
        }
        Timber.d("After filtering: ${filteredNewList.size} items")

        val diff = Diff(appsList, filteredNewList)
        val result = DiffUtil.calculateDiff(diff)

        appsList.clear()
        appsList.addAll(filteredNewList)

        result.dispatchUpdatesTo(this)
    }

    fun setOriginalAppsList(newList: List<AppItem>) {
        originalAppsList.clear()
        originalAppsList.addAll(newList)
    }

    interface OnModeChangeListener {
        fun onModeChange(packageName: String, newMode: Mode)
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val layout: ConstraintLayout = itemView.findViewById(R.id.item_layout)
        val mode: TextView = itemView.findViewById(R.id.mode)
    }

    class Diff(private val oldList: List<AppItem>, private val newList: List<AppItem>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }

    data class AppItem(
        val name: String,
        val packageName: String,
        val icon: Drawable?,
        val mode: Mode,
        val isSystem: Boolean
    )
}