package com.omartech.emrtd_core.ui

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.omartech.emrtd_core.EPassConfig
import com.omartech.emrtd_core.ErrorCode
import com.omartech.emrtd_core.PassportResult
import com.omartech.emrtd_core.data.chip.JmrtdChipReaderRepo
import com.omartech.emrtd_core.domain.model.MrzInfo
import com.omartech.emrtd_core.domain.model.PassportData
import com.omartech.emrtd_core.domain.repository.ChipReadResult
import com.omartech.emrtd_core.ui.screen.MrzScanScreen
import com.omartech.emrtd_core.ui.screen.TapNfcScreen
import com.omartech.emrtd_core.util.NfcForegroundDispatcher
import com.omartech.emrtd_core.util.parcelable
import kotlinx.coroutines.launch

internal class PassportFlowActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_CFG = "cfg"
        private var resultCb: ((PassportResult) -> Unit)? = null

        fun launch(host: Activity, cfg: EPassConfig, cb: (PassportResult) -> Unit) {
            resultCb = cb
            host.startActivity(
                Intent(host, PassportFlowActivity::class.java).apply {
                    putExtra(EXTRA_CFG, cfg) // now Parcelable
                }
            )
        }
    }

    private var mrzInfo: MrzInfo? = null
    private lateinit var cfg: EPassConfig
    private var nfc: NfcForegroundDispatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Safely read Parcelable on all API levels
        cfg = intent.parcelable<EPassConfig>(EXTRA_CFG) ?: EPassConfig()

        nfc = NfcForegroundDispatcher(this)

        setContent {
            if (mrzInfo == null) {
                MrzScanScreen(
                    onMrz = { mrz ->
                        mrzInfo = mrz
                        setContent { TapNfcScreen() }
                    },
                    onCancel = { finishWith(PassportResult.Cancelled("user")) }
                )
            } else {
                TapNfcScreen()
            }
        }
    }

    override fun onResume() { super.onResume(); nfc?.enable() }
    override fun onPause() { nfc?.disable(); super.onPause() }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Safely read the Tag on all API levels
        val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        val mrz = mrzInfo ?: return
        val nfcTag = tag ?: return

        lifecycleScope.launch {
            val reader = JmrtdChipReaderRepo(cfg)
            when (val out = reader.read(nfcTag, mrz)) {
                is ChipReadResult.Success -> {
                    finishWith(
                        PassportResult.Success(
                            PassportData(
                                mrz = mrz,
                                sodVerified = out.sodVerified,
                                chipAuthPerformed = cfg.enablePACE,
                                dg1 = out.dg1,
                                dg11 = out.dg11,
                                dg12 = out.dg12,
                                portraitJpegOrJ2k = out.portrait
                            )
                        )
                    )
                }
                is ChipReadResult.Error ->
                    finishWith(PassportResult.Error(ErrorCode.ChipAccessFailed, out.msg, out.cause))
            }
        }
    }


    private fun finishWith(result: PassportResult) {
        resultCb?.invoke(result)
        resultCb = null
        finish()
    }
}
