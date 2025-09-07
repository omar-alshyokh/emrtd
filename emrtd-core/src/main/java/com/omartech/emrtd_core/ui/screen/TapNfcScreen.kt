package com.omartech.emrtd_core.ui.screen

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun TapNfcScreen(modifier: Modifier = Modifier) {
    // Minimal UI: title + guidance (customize as you like)
    Text("Hold your e-passport to the back of your phone", modifier = modifier)
}