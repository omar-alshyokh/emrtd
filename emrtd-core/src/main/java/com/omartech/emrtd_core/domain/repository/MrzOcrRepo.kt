package com.omartech.emrtd_core.domain.repository

import com.omartech.emrtd_core.domain.model.MrzInfo

interface MrzOcrRepo {
    suspend fun scanMrzOnce(timeoutMs: Long): Result<MrzInfo>
}