package com.omartech.emrtd_core.domain.repository

import android.nfc.Tag
import com.omartech.emrtd_core.domain.model.Dg1Basic
import com.omartech.emrtd_core.domain.model.Dg11Extras
import com.omartech.emrtd_core.domain.model.Dg12Extras
import com.omartech.emrtd_core.domain.model.MrzInfo

sealed interface ChipReadResult {

    data class Success(
        val dg1: Dg1Basic?,
        val portrait: ByteArray?,      // DG2 bytes (JPEG or JPEG2000)
        val sodVerified: Boolean,      // TODO: wire real Passive Auth
        val dg11: Dg11Extras? = null,
        val dg12: Dg12Extras? = null,
        val paceDone: Boolean          // <-- NEW: true if PACE succeeded, false if fell back to BAC
    ) : ChipReadResult

    data class Error(val msg: String, val cause: Throwable? = null) : ChipReadResult
}

interface ChipReaderRepo {
    suspend fun read(tag: Tag, mrz: MrzInfo): ChipReadResult
}

/** Convenience when you construct Success first, then add DG11/DG12. */
fun ChipReadResult.Success.withExtras(dg11: Dg11Extras?, dg12: Dg12Extras?) =
    copy(dg11 = dg11, dg12 = dg12)
