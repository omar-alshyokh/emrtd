package com.omartech.emrtd_core.ui.screen

import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.omartech.emrtd_core.data.mrz.Mrz
import com.omartech.emrtd_core.domain.model.MrzInfo

import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
internal fun MrzScanScreen(
    onMrz: (MrzInfo) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { PreviewView(it).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        } }
    ) { previewView ->
        val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            val mediaImage = imageProxy.image ?: return@setAnalyzer
            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(input)
                .addOnSuccessListener { result ->
                    val lines = result.text.split('\n').map { it.trim().replace(' ', '<').uppercase() }
                    Mrz.parse(lines)?.let {
                        imageProxy.close()
                        cameraProvider.unbindAll()
                        onMrz(MrzInfo(it.docNumber, it.dob, it.doe))
                    } ?: imageProxy.close()
                }
                .addOnFailureListener { imageProxy.close() }
        }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(ctx as LifecycleOwner, selector, preview, analysis)
    }
}