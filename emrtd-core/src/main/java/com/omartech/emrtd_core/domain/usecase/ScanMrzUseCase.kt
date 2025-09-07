package com.omartech.emrtd_core.domain.usecase

import com.omartech.emrtd_core.domain.model.MrzInfo
import com.omartech.emrtd_core.domain.repository.MrzOcrRepo

class ScanMrzUseCase(private val repo: MrzOcrRepo) {
    suspend operator fun invoke(timeoutMs: Long): Result<MrzInfo> = repo.scanMrzOnce(timeoutMs)
}