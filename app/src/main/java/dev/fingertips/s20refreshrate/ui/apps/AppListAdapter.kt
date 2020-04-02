package dev.fingertips.s20refreshrate.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import dev.fingertips.s20refreshrate.R
import dev.fingertips.s20refreshrate.db.App
import dev.fingertips.s20refreshrate.db.Mode
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AppListAdapter @Inject constructor(
    private val context: Context
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {
    private val appsList = ArrayList<PackageInfo>()
    private val selectedAppsList = ArrayList<App>()

    private var previousExpandedPosition = -1
    private var expandedPosition = -1

    private var selectedAppsUpdated = false

    var onModeChangeListener: OnModeChangeListener? = null

    override fun getItemCount(): Int {
        return if (selectedAppsUpdated && appsList.size > 0) {
            appsList.size
        } else {
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        appsList[position].let { info ->
            holder.appName.text = info.applicationInfo.loadLabel(context.packageManager)
            holder.icon.setImageDrawable(info.applicationInfo.loadIcon(context.packageManager))

            // Mode text
            var modeText = context.getString(R.string.default_hz)
            selectedAppsList.firstOrNull { it.packageName == info.packageName }.let { app ->
                if (app != null) {
                    when (app.mode) {
                        Mode.DEFAULT -> {
                            modeText = context.getString(R.string.default_hz)
                            holder.buttonDefault.isChecked = true
                        }
                        Mode.SIXTY -> {
                            modeText = context.getString(R.string.sixty_hz)
                            holder.button60Hz.isChecked = true
                        }
                        Mode.ONE_TWENTY -> {
                            modeText = context.getString(R.string.one_twenty_hz)
                            holder.button120hz.isChecked = true
                        }
                    }
                }
            }
            holder.mode.text = modeText

            // Expanding list item on click
            val isExpanded = position == expandedPosition
            holder.buttonLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.itemView.isActivated = isExpanded

            if (isExpanded) previousExpandedPosition = position

            holder.layout.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(position)
            }

            // On radio click
            holder.buttonLayout.setOnCheckedChangeListener { _, checkedId ->
                if (onModeChangeListener != null) {
                    val newMode = when (checkedId) {
                        R.id.radio_60_hz -> Mode.SIXTY
                        R.id.radio_120_hz -> Mode.ONE_TWENTY
                        else -> Mode.DEFAULT
                    }

                    onModeChangeListener?.onModeChange(info.packageName, newMode)
                }
            }
        }
    }

    fun getItemTitle(position: Int): String {
        return appsList[position].applicationInfo.loadLabel(context.packageManager).toString()
    }

    fun updateAppsList(newList: List<PackageInfo>) {
        appsList.clear()
        appsList.addAll(newList.filter { !isSystemApp(it) }.sortedBy {
            it.applicationInfo.loadLabel(context.packageManager).toString()
                .toLowerCase(Locale.getDefault())
        })
        notifyDataSetChanged()
    }

    fun updateSelectedAppsList(newList: List<App>) {
        selectedAppsList.clear()
        selectedAppsList.addAll(newList)
        selectedAppsUpdated = true
        notifyDataSetChanged()
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
        val buttonLayout: RadioGroup = itemView.findViewById(R.id.button_layout)
        val buttonDefault: RadioButton = itemView.findViewById(R.id.radio_default)
        val button60Hz: RadioButton = itemView.findViewById(R.id.radio_60_hz)
        val button120hz: RadioButton = itemView.findViewById(R.id.radio_120_hz)
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val layout: ConstraintLayout = itemView.findViewById(R.id.item_layout)
        val mode: TextView = itemView.findViewById(R.id.mode)
    }
}