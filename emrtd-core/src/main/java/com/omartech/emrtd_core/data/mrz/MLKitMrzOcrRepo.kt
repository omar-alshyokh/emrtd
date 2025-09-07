package com.omartech.emrtd_core.data.mrz

import com.omartech.emrtd_core.domain.model.MrzInfo
import com.omartech.emrtd_core.domain.repository.MrzOcrRepo

internal class MLKitMrzOcrRepo : MrzOcrRepo {
    override suspend fun scanMrzOnce(timeoutMs: Long): Result<MrzInfo> {
        return Result.failure(UnsupportedOperationException("MRZ OCR is driven by UI analyzer in MrzScanScreen"))
    }
}