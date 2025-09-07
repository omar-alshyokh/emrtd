package com.omartech.emrtd_core

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import com.omartech.emrtd_core.core.PassportCoordinator
import com.omartech.emrtd_core.domain.model.PassportData
import com.omartech.emrtd_core.image.PortraitDecoder
import kotlinx.parcelize.Parcelize


object EMRTD {
    @Volatile internal var portraitDecoder: PortraitDecoder? = null

    fun start(
        host: Activity,
        config: EPassConfig = EPassConfig(),
        callback: (PassportResult) -> Unit,
        decoder: PortraitDecoder? = null
    ) {
        portraitDecoder = decoder
        PassportCoordinator.start(host, config.copy(portraitDecoderProvided = decoder != null), callback)
    }
}


@Parcelize
data class EPassConfig(
    val dataGroups: List<DataGroup> = listOf(DataGroup.DG1, DataGroup.DG2, DataGroup.DG11, DataGroup.DG12),
    val requirePassiveAuth: Boolean = true,
    val enablePACE: Boolean = true,
    val ocrTimeoutMs: Long = 25_000,
    val nfcTimeoutMs: Int = 20_000,
    val returnPortrait: Boolean = true,
    /** Optional JPEG2000 decoder. If null, library will only try JPEG via BitmapFactory. */
    val portraitDecoderProvided: Boolean = false // parcel-friendly flag (actual impl is injected at runtime)
) : Parcelable

enum class DataGroup { DG1, DG2, DG11, DG12 }

sealed interface PassportResult {
    data class Success(val data: PassportData): PassportResult
    data class Cancelled(val reason: String? = null): PassportResult
    data class Error(val code: ErrorCode, val message: String? = null, val cause: Throwable? = null): PassportResult
}

enum class ErrorCode { CameraUnavailable, OcrTimeout, NfcUnavailable, NfcTimeout, MrzInvalid, ChipAccessFailed, AuthFailed, Unknown }

