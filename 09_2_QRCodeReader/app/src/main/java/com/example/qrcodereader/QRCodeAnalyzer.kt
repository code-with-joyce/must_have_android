package com.example.qrcodereader

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


class QRCodeAnalyzer(val onDetectListener: OnDetectListener)  : ImageAnalysis.Analyzer{ // ❶

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { qrCodes ->
                    for (qrCode in qrCodes) {
                        onDetectListener.onDetect(qrCode.rawValue ?: "")
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
