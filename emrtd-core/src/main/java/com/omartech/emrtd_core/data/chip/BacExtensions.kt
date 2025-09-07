package com.omartech.emrtd_core.data.chip

import com.omartech.emrtd_core.domain.model.MrzInfo
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec

internal fun MrzInfo.toBacKey(): BACKeySpec =
    BACKey(documentNumber, dateOfBirthYYMMDD, dateOfExpiryYYMMDD)