package com.omartech.emrtd_core.domain.usecase

import android.nfc.Tag
import com.omartech.emrtd_core.domain.model.MrzInfo
import com.omartech.emrtd_core.domain.repository.ChipReadResult
import com.omartech.emrtd_core.domain.repository.ChipReaderRepo

class ReadChipUseCase(private val repo: ChipReaderRepo) {
    suspend operator fun invoke(tag: Tag, mrz: MrzInfo): ChipReadResult = repo.read(tag, mrz)
}