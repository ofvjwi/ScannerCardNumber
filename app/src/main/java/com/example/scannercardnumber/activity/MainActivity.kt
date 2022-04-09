package com.example.scannercardnumber.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.scannercardnumber.R
import com.example.scannercardnumber.databinding.ActivityMainBinding
import com.example.scannercardnumber.extensions.ActivityExtensions.toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private lateinit var binding: ActivityMainBinding
    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder().build().also {
            it.setAnalyzer(cameraExecutor, TextReaderAnalyzer(::onTextFound))
        }
    }
    private lateinit var camera: Camera
    private var status: Boolean = false

    private companion object {
        private const val TAG = "MainActivity"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        if (allPermissionsGranted()) startCamera() else requestPermissions()
        binding.lightButton.setOnClickListener { changeFlashLightState(!status) }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun fullScreen(context: Context) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(context, R.color.red)
    }

    private fun changeFlashLightState(status: Boolean) {
        camera.cameraControl.enableTorch(status) // or false
        this.status = status
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera() else {
                toast("Permissions not granted.")
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
                }

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    camera =
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

                } catch (illegalStateException: IllegalStateException) {
                    // If the use case has already been bound to another lifecycle or method is not called on main thread.
                    Log.e(TAG, illegalStateException.message.orEmpty())
                } catch (illegalArgumentException: IllegalArgumentException) {
                    // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
                    Log.e(TAG, illegalArgumentException.message.orEmpty())
                } catch (exception: Exception) {
                    Log.e(TAG, exception.message.orEmpty())
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private class TextReaderAnalyzer(private val textFoundListener: (String) -> Unit) :
        ImageAnalysis.Analyzer {

        companion object {
            private const val TAG: String = "TextReaderAnalyzer"
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            imageProxy.image?.let {
                process(it, imageProxy)
            }
        }

        private fun process(image: Image, imageProxy: ImageProxy) {
            try {
                readTextFromImage(InputImage.fromMediaImage(image, 90), imageProxy)
            } catch (e: IOException) {
                Log.d(TAG, "processIOException: Failed to load the image")
                e.printStackTrace()
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        private fun readTextFromImage(image: InputImage, imageProxy: ImageProxy) {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener { visionText ->
                    processTextFromImage(
                        visionText,
                        /**imageProxy*/
                    )
                    Log.d(TAG, "readTextFromImageVisionText: Text: ${visionText.text.lines()}")
                    imageProxy.close()
                }
                .addOnFailureListener { error ->
                    Log.d(TAG, "addOnFailureListener: Failed to process the image")
                    error.printStackTrace()
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }

        private fun processTextFromImage(
            visionText: Text,
            /**imageProxy: ImageProxy*/
        ) {
            textFoundListener(visionText.text)

            /**
            for (block in visionText.textBlocks) {
            // You can access whole block of text using block.text
            for (line in block.lines) {
            // You can access whole line of text using line.text
            for (element in line.elements) {
            // You can access whole elements of line using element.text
            }
            }
            }
             */
        }
    }

    private fun onTextFound(foundText: String) {
        binding.info.text = foundText
    }
}

