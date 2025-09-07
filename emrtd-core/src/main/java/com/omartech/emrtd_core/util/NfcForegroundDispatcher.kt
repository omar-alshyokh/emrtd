package com.omartech.emrtd_core.util

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import androidx.activity.ComponentActivity

internal class NfcForegroundDispatcher(private val activity: ComponentActivity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    private val pendingIntent: PendingIntent by lazy {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_MUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            flags
        )
    }

    fun enable() {
        // techLists must be array of array of fully-qualified tech class names (Strings)
        val techLists = arrayOf(
            arrayOf(android.nfc.tech.IsoDep::class.java.name)
        )
        adapter?.enableForegroundDispatch(activity, pendingIntent, null, techLists)
    }

    fun disable() {
        adapter?.disableForegroundDispatch(activity)
    }
}