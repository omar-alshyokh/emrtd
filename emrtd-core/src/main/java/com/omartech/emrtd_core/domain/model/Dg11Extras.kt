package com.omartech.emrtd_core.domain.model

data class Dg11Extras(
    val fullNameOfHolder: String? = null,
    val otherNames: List<String> = emptyList(),
    val personalNumber: String? = null,
    val dateOfBirthFull: String? = null,   // if present in DG11
    val placeOfBirth: String? = null,
    val permanentAddress: String? = null,
    val telephone: String? = null,
    val profession: String? = null,
    val title: String? = null,
    val personalSummary: String? = null
)
