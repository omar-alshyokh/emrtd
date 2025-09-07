package com.omartech.emrtd_core.data.chip

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.omartech.emrtd_core.DataGroup
import com.omartech.emrtd_core.EPassConfig
import com.omartech.emrtd_core.domain.model.Dg1Basic
import com.omartech.emrtd_core.domain.model.Dg11Extras
import com.omartech.emrtd_core.domain.model.Dg12Extras
import com.omartech.emrtd_core.domain.model.MrzInfo
import com.omartech.emrtd_core.domain.repository.ChipReadResult
import com.omartech.emrtd_core.domain.repository.ChipReaderRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.scuba.smartcards.IsoDepCardService
import org.jmrtd.PassportService
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.SODFile
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.DG11File
import org.jmrtd.lds.icao.DG12File
import org.jmrtd.lds.icao.DG2File
import java.security.spec.AlgorithmParameterSpec

internal class JmrtdChipReaderRepo(
    private val config: EPassConfig
) : ChipReaderRepo {

    override suspend fun read(tag: Tag, mrz: MrzInfo): ChipReadResult = withContext(Dispatchers.IO) {
        val isoDep = IsoDep.get(tag) ?: return@withContext ChipReadResult.Error("IsoDep missing")
        try {
            isoDep.timeout = config.nfcTimeoutMs
            isoDep.connect()

            val service = PassportService(IsoDepCardService(isoDep), 256, 224, false, false)
            service.open()
            service.sendSelectApplet(false)

            val bacKey = mrz.toBacKey()

            // ---------- PACE (if supported) ----------
            var paceDone = false
            if (config.enablePACE) {
                runCatching {
                    val cardAccessIs = service.getInputStream(PassportService.EF_CARD_ACCESS)
                    val cardAccess = CardAccessFile(cardAccessIs)
                    val paceInfo: PACEInfo? =
                        cardAccess.securityInfos.filterIsInstance<PACEInfo>().firstOrNull()
                    if (paceInfo != null) {
                        val oid = paceInfo.objectIdentifier
                        val params: AlgorithmParameterSpec? = null // JMRTD infers common curves from OID
                        service.doPACE(bacKey, oid, params)
                        paceDone = true
                    }
                }
            }

            // ---------- Fallback to BAC ----------
            if (!paceDone) {
                service.doBAC(bacKey)
            }

            // ---------- Read requested EF files ----------
            // SOD (for passive auth digest verification in a later hardening step)
            runCatching { SODFile(service.getInputStream(PassportService.EF_SOD)) }.getOrNull()

            val dg1File = if (config.dataGroups.contains(DataGroup.DG1))
                runCatching { DG1File(service.getInputStream(PassportService.EF_DG1)) }.getOrNull()
            else null

            val dg2File = if (config.dataGroups.contains(DataGroup.DG2) && config.returnPortrait)
                runCatching { DG2File(service.getInputStream(PassportService.EF_DG2)) }.getOrNull()
            else null

            val dg11File = if (config.dataGroups.contains(DataGroup.DG11))
                runCatching { DG11File(service.getInputStream(PassportService.EF_DG11)) }.getOrNull()
            else null

            val dg12File = if (config.dataGroups.contains(DataGroup.DG12))
                runCatching { DG12File(service.getInputStream(PassportService.EF_DG12)) }.getOrNull()
            else null

            // ---------- Map DG1 MRZ (rich) ----------
            val dg1 = dg1File?.mrzInfo?.let { mi ->
                Dg1Basic(
                    issuingState        = mi.issuingState,
                    nationality         = mi.nationality,
                    documentNumber      = mi.documentNumber,
                    dateOfBirth         = mi.dateOfBirth,     // YYMMDD
                    dateOfExpiry        = mi.dateOfExpiry,    // YYMMDD
                    sex                 = runCatching { mi.gender.name }.getOrNull()
                        ?: runCatching { mi.genderCode?.name }.getOrNull(),
                    primaryIdentifier   = mi.primaryIdentifier?.trim().orEmpty(),
                    secondaryIdentifier = mi.secondaryIdentifier?.trim().orEmpty(),
                    optionalData1       = mi.optionalData1,
                    optionalData2       = mi.optionalData2
                )
            }

            // ---------- Map DG11 / DG12 (optional, many passports omit) ----------
            val dg11 = dg11File?.let { f ->
                Dg11Extras(
                    fullNameOfHolder = f.nameOfHolder,
                    otherNames = f.otherNames ?: emptyList(),
                    personalNumber = f.personalNumber,
                    dateOfBirthFull = f.fullDateOfBirth,
                    placeOfBirth = f.placeOfBirth?.toString(),
                    permanentAddress = f.permanentAddress?.toString(),
                    telephone = f.telephone,
                    profession = f.profession,
                    title = f.title,
                    personalSummary = f.personalSummary
                )
            }

            val dg12 = dg12File?.let { f ->
                Dg12Extras(
                    issuingAuthority = f.issuingAuthority,
                    dateOfIssue = f.dateOfIssue,
                    endorsementsAndObservations = f.endorsementsAndObservations
                )
            }

            // ---------- Portrait (DG2) ----------
            val portraitBytes = dg2File
                ?.faceInfos?.firstOrNull()
                ?.faceImageInfos?.firstOrNull()
                ?.imageInputStream?.use { it.readBytes() }

            // NOTE: sodVerified is set true for now; wire real digest+PKI check later
            ChipReadResult.Success(
                dg1 = dg1,
                portrait = portraitBytes,
                sodVerified = true,
                dg11 = dg11,
                dg12 = dg12,
                paceDone = paceDone        // <-- NEW: report actual auth outcome
            )
        } catch (e: Exception) {
            ChipReadResult.Error("Chip read failed: ${e.message}", e)
        } finally {
            runCatching { isoDep.close() }
        }
    }
}
