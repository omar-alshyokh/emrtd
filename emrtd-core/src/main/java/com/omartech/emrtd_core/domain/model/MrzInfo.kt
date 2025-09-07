package com.omartech.emrtd_core.domain.model

data class MrzInfo(
    val documentNumber: String,
    val dateOfBirthYYMMDD: String,
    val dateOfExpiryYYMMDD: String
)
