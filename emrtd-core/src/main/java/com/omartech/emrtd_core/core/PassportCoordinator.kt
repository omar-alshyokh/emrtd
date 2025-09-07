package com.omartech.emrtd_core.core
import android.app.Activity
import android.content.Intent
import com.omartech.emrtd_core.EPassConfig
import com.omartech.emrtd_core.PassportResult
import com.omartech.emrtd_core.ui.PassportFlowActivity

internal object PassportCoordinator {
    fun start(host: Activity, cfg: EPassConfig, cb: (PassportResult) -> Unit) {
        PassportFlowActivity.launch(host, cfg, cb)
    }
}