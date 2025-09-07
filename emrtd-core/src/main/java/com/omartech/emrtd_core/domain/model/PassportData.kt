package com.omartech.emrtd_core.domain.model

data class PassportData(
    val mrz: MrzInfo,
    val sodVerified: Boolean,
    val chipAuthPerformed: Boolean?,
    val dg1: Dg1Basic?,
    val dg11: Dg11Extras? = null,
    val dg12: Dg12Extras? = null,
    val portraitJpegOrJ2k: ByteArray?
)
