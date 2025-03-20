package com.example.fastaf.ui.cam
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fastaf.databinding.ActivityCamBinding
import com.example.fastaf.ui.api.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    private val cameraPermission = Manifest.permission.CAMERA
    private val requestCodeCamera = 100
    private var drugId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamBinding.inflate(layoutInflater)
        setContentView(binding.root)


        drugId = intent.getIntExtra("DRUG_ID", -1)
        if (drugId == -1) {
            Log.e("IDPASSEDSUCCESS", "No ID Passed")
            Toast.makeText(this, "No drug id provided!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        checkCameraPermission()

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureButton.setOnClickListener {
            capturePhoto()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCodeCamera)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCamera && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        val file = File(cacheDir, "captured_image.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    showConfirmDialog(bitmap, file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CamActivity, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showConfirmDialog(photo: Bitmap, file: File) {
        val dialog = ConfirmDialogFragment(
            photo,
            onConfirm = { uploadImage(file) },
            onRetry = {
                startCamera()
            }
        )
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    private fun uploadImage(file: File) {
        binding.uploadProgress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                val response = ApiManager.getWebService().uploadImage((drugId ?: 0), filePart)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("UploadSuccess", "Response: ${response.body()}")
                        Toast.makeText(this@CamActivity, "Upload successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("UploadFailed", "Response code: ${response.code()}, message: ${response.message()}")

                        Log.e("UploadFailed", "Error body: ${response.errorBody()?.string()}")

                        Toast.makeText(this@CamActivity, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UploadError", "Exception: ${e.message}")
                    Toast.makeText(this@CamActivity, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.uploadProgress.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))

}
}