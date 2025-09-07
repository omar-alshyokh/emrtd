package com.omartech.emrtd_core.domain.model


data class Dg1Basic(
    val issuingState: String?,          // from MRZ
    val nationality: String?,
    val documentNumber: String,
    val dateOfBirth: String,            // YYMMDD
    val dateOfExpiry: String,           // YYMMDD
    val sex: String?,                   // "M","F","X" (JMRTD MRZInfo.gender)
    val primaryIdentifier: String,      // surname(s)
    val secondaryIdentifier: String,    // given name(s)
    val optionalData1: String?,         // MRZ optional field(s)
    val optionalData2: String?
)

