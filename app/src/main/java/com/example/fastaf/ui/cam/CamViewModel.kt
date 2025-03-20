package com.example.fastaf.ui.cam

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CamViewModel:ViewModel() {

    private val _showConfirmDialog = MutableLiveData<Bitmap?>()
    val showConfirmDialog: LiveData<Bitmap?> = _showConfirmDialog
    private val _showDialog = MutableLiveData<Bitmap?>()
    val showDialog: LiveData<Bitmap?> get() = _showDialog

    private val _uploadResult = MutableLiveData<String?>()
    val uploadResult: LiveData<String?> = _uploadResult

    fun capturePhoto(bitmap: Bitmap) {
        _showConfirmDialog.value = bitmap
    }

    fun resetDialog() {
        _showConfirmDialog.value = null
    }

    fun uploadPhoto(bitmap: Bitmap, drugId: Int) {
        Log.d("CamViewModel", "Uploading photo for drugId: $drugId")
        viewModelScope.launch {
            try {
                val file = bitmapToFile(bitmap)
                val requestBody = file.asRequestBody()
                val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = ApiManager.getWebService().uploadImage(drugId, multipartBody)
                Log.d("uploadsuccess", "Uploading photo for drugId: $drugId")
                if (response.isSuccessful) {
                    _uploadResult.value = "Image uploaded successfully!"
                } else {
                    _uploadResult.value = "Upload failed: ${response.message()}"
                }

            } catch (e: Exception) {
                Log.e("CamViewModel", "Upload error", e)
                _uploadResult.value = "Error: ${e.message}"
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File.createTempFile("temp_image", ".jpg")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return file
    }
}