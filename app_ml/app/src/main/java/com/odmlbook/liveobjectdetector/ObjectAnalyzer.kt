package com.odmlbook.liveobjectdetector

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions


class ObjectAnalyzer(graphicOverlay: GraphicOverlay) : ImageAnalysis.Analyzer {

    val localModel = LocalModel.Builder()
        .setAssetFilePath("model_meta.tflite")
        .build()

    // Live detection and tracking
    val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .setClassificationConfidenceThreshold(0.9f)
            .setMaxPerObjectLabelCount(3)
            .build()



    val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)


    val overlay = graphicOverlay
    private val lensFacing = CameraSelector.LENS_FACING_BACK

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            overlay.setImageSourceInfo(
                imageProxy.width, imageProxy.height, isImageFlipped
            )
        } else {
            overlay.setImageSourceInfo(
                imageProxy.height, imageProxy.width, isImageFlipped
            )
        }
        val frame = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
        objectDetector.process(frame)
                .addOnSuccessListener { detectedObjects ->
                    // Task completed successfully
                    overlay.clear()
                    for (detectedObject in detectedObjects){
                        val objGraphic = ObjectGraphic(this.overlay, detectedObject)
                        this.overlay.add(objGraphic)
                    }
                    this.overlay.postInvalidate()

                }

                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
                .addOnCompleteListener {
                    imageProxy.close()

                }


    }

}

