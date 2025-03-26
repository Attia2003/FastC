package com.example.fastaf.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.util.PrefsUtils
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val userName = MutableLiveData<String>()
    val errorUserName = MutableLiveData<String>()
    val isAdmin = MutableLiveData<Boolean>()
    val loggedInUser = MutableLiveData<String>()

    fun login(context: Context) {
        val name = userName.value.orEmpty()
        if (name.isEmpty()) {
            errorUserName.postValue("Username cannot be empty!")
            return
        }
        viewModelScope.launch {
            try {
                val response = ApiManager.getWebService().getUserInfo(name)
                if (response.isSuccessful && response.body() != null) {
                    val userInfo = response.body()!!
                    isAdmin.postValue(userInfo.role == "admin")

                    PrefsUtils.saveUserToPrefs(context, name)
                    PrefsUtils.saveUserRole(context, userInfo.role)
                    loggedInUser.postValue(name)

                    Log.d("LoginViewModel", "Saved role: ${userInfo.role}")

                } else {
                    errorUserName.postValue("User not found or error occurred!")
                }
            } catch (e: Exception) {
                errorUserName.postValue("Network error: ${e.message}")
            }
        }
    }

}