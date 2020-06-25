package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.db.Mode
import timber.log.Timber
import javax.inject.Inject

class AppListAdapter @Inject constructor(
    private val context: Context
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {
    private val appsList = mutableListOf<AppItem>()

    private var onClickListener: (packageName: String) -> Unit = {}

    var onModeChangeListener: OnModeChangeListener? = null

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
                Mode.ONE_TWENTY -> context.getString(R.string.one_twenty_hz)
                else -> context.getString(R.string.default_hz)
            }

            holder.layout.setOnClickListener {
                onClickListener.invoke(appItem.packageName)
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

    fun updateAppsList(newList: List<AppItem>) {
        Timber.d("Received ${newList.size} new AppItems")
        val diff = Diff(appsList, newList)
        val result = DiffUtil.calculateDiff(diff)

        appsList.clear()
        appsList.addAll(newList)
        result.dispatchUpdatesTo(this)
    }

    private fun isSystemApp(packageInfo: PackageInfo): Boolean {
        return false
        // return ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
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
        val mode: Mode
    )
}