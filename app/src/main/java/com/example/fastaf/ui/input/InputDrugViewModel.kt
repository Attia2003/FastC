package com.example.fastaf.ui.input

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import kotlinx.coroutines.launch

class InputDrugViewModel : ViewModel() {

    val userName = MutableLiveData<String>()
    val selectedForm = MutableLiveData<String>()
    val isSuccess = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> get() = _navigateToMain

    fun createDrug() {
        val name = userName.value
        val form = selectedForm.value

        if (name.isNullOrBlank() || form.isNullOrBlank()) {
            errorMessage.value = "Please enter a drug name and select a form."
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            try {
                val response = ApiManager.getWebService().createDRUG(
                    DrugCreationRequest(name, form)
                )
                if (response.isSuccessful) {
                    isSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("API_ERROR", "Failed to create drug: $errorBody")
                    errorMessage.value = "Failed to create drug: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Error: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    fun onBackToMainClick() {
        _navigateToMain.value = true
    }
}