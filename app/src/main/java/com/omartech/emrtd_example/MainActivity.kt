package com.omartech.emrtd_example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omartech.emrtd_core.EMRTD
import com.omartech.emrtd_core.EPassConfig
import com.omartech.emrtd_core.PassportResult
import com.omartech.emrtd_core.domain.model.PassportData
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PassportDemoScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassportDemoScreen() {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }

    var data by remember { mutableStateOf<PassportData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            error = "Camera permission is required to scan the MRZ."
            return@rememberLauncherForActivityResult
        }

        val cfg = EPassConfig(
            requirePassiveAuth = true,
            enablePACE = true,
            ocrTimeoutMs = 25_000,
            nfcTimeoutMs = 20_000,
            returnPortrait = true
        )

        EMRTD.start(
            host = activity,
            config = cfg,
            callback = { result ->
                when (result) {
                    is PassportResult.Success -> { data = result.data; error = null }
                    is PassportResult.Error   -> { data = null; error = result.message ?: result.code.toString() }
                    is PassportResult.Cancelled -> { data = null; error = "Cancelled" }
                }
            }
            // decoder = null   // optional
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("e-Passport Demo") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { requestCameraPermission.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Scan e-Passport") }

            error?.let { Text(text = "Status: $it", color = MaterialTheme.colorScheme.error) }

            data?.let { pd ->
                // ---------- Summary ----------
                Text(
                    "SOD: ${if (pd.sodVerified) "Verified" else "Not verified"}",
                    style = MaterialTheme.typography.titleMedium
                )

                // ---------- DG1 ----------
                val dg1 = pd.dg1
                if (dg1 != null) {
                    SectionHeader("DG1 (MRZ)")
                    L("Primary Identifier", dg1.primaryIdentifier)
                    L("Secondary Identifier", dg1.secondaryIdentifier)
                    L("Issuing State", dg1.issuingState)
                    L("Nationality", dg1.nationality)
                    L("Document #", dg1.documentNumber)
                    L("Date of Birth (YYMMDD)", dg1.dateOfBirth)
                    L("Date of Expiry (YYMMDD)", dg1.dateOfExpiry)
                    L("Sex", dg1.sex)
                    if (!dg1.optionalData1.isNullOrBlank()) L("Optional Data 1", dg1.optionalData1)
                    if (!dg1.optionalData2.isNullOrBlank()) L("Optional Data 2", dg1.optionalData2)
                }

                // ---------- DG11 ----------
                val dg11 = pd.dg11
                if (dg11 != null) {
                    SectionHeader("DG11 (Additional Personal Details)")
                    L("Full Name", dg11.fullNameOfHolder)
                    if (dg11.otherNames.isNotEmpty()) L("Other Names", dg11.otherNames.joinToString())
                    L("Personal Number", dg11.personalNumber)
                    L("DOB (Full)", dg11.dateOfBirthFull)
                    L("Place of Birth", dg11.placeOfBirth)
                    L("Permanent Address", dg11.permanentAddress)
                    L("Telephone", dg11.telephone)
                    L("Profession", dg11.profession)
                    L("Title", dg11.title)
                    L("Personal Summary", dg11.personalSummary)
                }

                // ---------- DG12 ----------
                val dg12 = pd.dg12
                if (dg12 != null) {
                    SectionHeader("DG12 (Additional Document Details)")
                    L("Issuing Authority", dg12.issuingAuthority)
                    L("Date of Issue", dg12.dateOfIssue)
                    L("Endorsements/Observations", dg12.endorsementsAndObservations)
                }

                // ---------- Portrait (DG2) ----------
                SectionHeader("Portrait")
                val img: ImageBitmap? = remember(pd.portraitJpegOrJ2k) {
                    pd.portraitJpegOrJ2k?.let { bytes ->
                        // Try standard JPEG decode first
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                }
                if (img != null) {
                    Image(bitmap = img, contentDescription = "Portrait", modifier = Modifier.size(180.dp))
                } else if (pd.portraitJpegOrJ2k != null) {
                    Text("Portrait present but not displayable (likely JPEG2000). Add a JP2 decoder to render.")
                } else {
                    Text("No portrait in DG2.")
                }

                // ---------- Export JSON ----------
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val json = toPrettyJson(pd)
                        val file = File(context.cacheDir, "epass_dump.json").apply { writeText(json) }
                        Toast.makeText(context, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export all data (JSON)") }
            }
        }
    }
}

/* ---------- small UI helpers ---------- */

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Divider()
}

@Composable
private fun L(label: String, value: String?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$label:")
        Text(value ?: "—", maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/* ---------- helpers ---------- */

// Get the hosting Activity from a Compose context safely.
private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Activity not found from context.")
}

// Minimal JSON builder (so you don’t need another dependency)
private fun toPrettyJson(pd: PassportData): String {
    fun esc(s: String?) = s?.replace("\\", "\\\\")?.replace("\"", "\\\"")
    val dg1 = pd.dg1
    val dg11 = pd.dg11
    val dg12 = pd.dg12
    val b64Portrait = pd.portraitJpegOrJ2k?.let {
        android.util.Base64.encodeToString(it, android.util.Base64.NO_WRAP)
    }

    return buildString {
        append("{\n")
        append("  \"sodVerified\": ${pd.sodVerified},\n")
        append("  \"chipAuthPerformed\": ${pd.chipAuthPerformed ?: false},\n")
        append("  \"mrz\": { \"docNumber\": \"${esc(pd.mrz.documentNumber)}\", \"dob\": \"${esc(pd.mrz.dateOfBirthYYMMDD)}\", \"doe\": \"${esc(pd.mrz.dateOfExpiryYYMMDD)}\" },\n")
        append("  \"dg1\": ")
        if (dg1 != null) {
            append("{")
            append("\"issuingState\":\"${esc(dg1.issuingState)}\",")
            append("\"nationality\":\"${esc(dg1.nationality)}\",")
            append("\"documentNumber\":\"${esc(dg1.documentNumber)}\",")
            append("\"dateOfBirth\":\"${esc(dg1.dateOfBirth)}\",")
            append("\"dateOfExpiry\":\"${esc(dg1.dateOfExpiry)}\",")
            append("\"sex\":\"${esc(dg1.sex)}\",")
            append("\"primaryIdentifier\":\"${esc(dg1.primaryIdentifier)}\",")
            append("\"secondaryIdentifier\":\"${esc(dg1.secondaryIdentifier)}\",")
            append("\"optionalData1\":\"${esc(dg1.optionalData1)}\",")
            append("\"optionalData2\":\"${esc(dg1.optionalData2)}\"")
            append("}")
        } else append("null")
        append(",\n  \"dg11\": ")
        if (dg11 != null) {
            append("{")
            append("\"fullNameOfHolder\":\"${esc(dg11.fullNameOfHolder)}\",")
            append("\"otherNames\":[${dg11.otherNames.joinToString { "\"${esc(it)}\"" }}],")
            append("\"personalNumber\":\"${esc(dg11.personalNumber)}\",")
            append("\"dateOfBirthFull\":\"${esc(dg11.dateOfBirthFull)}\",")
            append("\"placeOfBirth\":\"${esc(dg11.placeOfBirth)}\",")
            append("\"permanentAddress\":\"${esc(dg11.permanentAddress)}\",")
            append("\"telephone\":\"${esc(dg11.telephone)}\",")
            append("\"profession\":\"${esc(dg11.profession)}\",")
            append("\"title\":\"${esc(dg11.title)}\",")
            append("\"personalSummary\":\"${esc(dg11.personalSummary)}\"")
            append("}")
        } else append("null")
        append(",\n  \"dg12\": ")
        if (dg12 != null) {
            append("{")
            append("\"issuingAuthority\":\"${esc(dg12.issuingAuthority)}\",")
            append("\"dateOfIssue\":\"${esc(dg12.dateOfIssue)}\",")
            append("\"endorsementsAndObservations\":\"${esc(dg12.endorsementsAndObservations)}\"")
            append("}")
        } else append("null")
        append(",\n  \"portraitBase64\": ${if (b64Portrait != null) "\"$b64Portrait\"" else "null"}\n")
        append("}\n")
    }
}
