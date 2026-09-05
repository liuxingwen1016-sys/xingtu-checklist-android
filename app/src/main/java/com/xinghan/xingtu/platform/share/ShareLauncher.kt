package com.xinghan.xingtu.platform.share

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.xinghan.xingtu.R

/**
 * Launches the system share sheet with a plain-text trip summary.
 */
class ShareLauncher(private val context: Context) {

    /** Returns true when the share sheet was actually presented. */
    fun shareText(text: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, context.getString(R.string.app_name))
        }
        val chooser = Intent.createChooser(intent, null)
        return try {
            context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show()
            false
        }
    }
}
