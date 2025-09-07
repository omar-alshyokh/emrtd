package com.omartech.emrtd_core.image

import android.graphics.Bitmap

/** Implement this if you add a JP2 (JPEG2000) decoder. Return null if decode fails. */
fun interface PortraitDecoder {
    fun decode(bytes: ByteArray): Bitmap?
}