package com.example.fastaf.ui.cam

 sealed class UploadStatus {
     object Success : UploadStatus()
     data class Error(val message: String) : UploadStatus()
}