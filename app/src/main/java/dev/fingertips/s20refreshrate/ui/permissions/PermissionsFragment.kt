package dev.fingertips.s20refreshrate.ui.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import d
import dev.fingertips.s20refreshrate.R
import kotlinx.android.synthetic.main.fragment_permissions.*

class PermissionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        d { "onCreateView" }
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        d { "onActivityCreated" }

        val acc = arguments?.getBoolean(ARG_ACC) ?: false
        val adb = arguments?.getBoolean(ARG_ADB) ?: false

        if (acc && adb) {
            // Both are set as not needed, this shouldn't happen
            throw IllegalStateException("Both acc and adb are false")
        }

        if (!adb) {
            // adb permission needs to be set, hide the accessibility views for now
            acc_text.visibility = View.GONE
            acc_button.visibility = View.GONE

            adb_text.visibility = View.VISIBLE
            adb_visit.visibility = View.VISIBLE
            adb_share.visibility = View.VISIBLE
        } else if (!acc) {
            // adb permission is set but we still need accessibility, hide the adb views
            adb_text.visibility = View.GONE
            adb_visit.visibility = View.GONE
            adb_share.visibility = View.GONE

            acc_text.visibility = View.VISIBLE
            acc_button.visibility = View.VISIBLE
        }

        acc_button.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Toast.makeText(requireContext(), R.string.permission_acc_toast, Toast.LENGTH_LONG).show()
        }

        adb_visit.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, INSTRUCTIONS_URI)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        }

        adb_share.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, INSTRUCTIONS_URL)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        restart_button.setOnClickListener {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    companion object {
        private const val ARG_ACC = "acc"
        private const val ARG_ADB = "adb"

        private const val INSTRUCTIONS_URL = "https://github.com/brericha/S20-Refresh-Rate/README.md"
        val INSTRUCTIONS_URI: Uri = Uri.parse(INSTRUCTIONS_URL)

        fun newInstance(acc: Boolean = false, adb: Boolean = false): PermissionsFragment {
            val fragment = PermissionsFragment()
            val args = Bundle()
            args.putBoolean(ARG_ACC, acc)
            args.putBoolean(ARG_ADB, adb)
            fragment.arguments = args
            return fragment
        }
    }
}