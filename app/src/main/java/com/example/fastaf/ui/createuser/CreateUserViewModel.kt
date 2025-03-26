package com.example.fastaf.ui.createuser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastaf.ui.api.ApiManager
import com.example.fastaf.ui.login.UserInfoResponse
import kotlinx.coroutines.launch

class CreateUserViewModel : ViewModel() {
    val userName = MutableLiveData<String>()
    val selectedRole = MutableLiveData<String>()
    val createUserStatus = MutableLiveData<String>()

    fun createUser() {
        val name = userName.value.orEmpty()
        val role = selectedRole.value.orEmpty()

        if (name.isEmpty()) {
            createUserStatus.postValue("Username cannot be empty!")
            return
        }

        if (role.isEmpty()) {
            createUserStatus.postValue("Please select a role!")
            return
        }

        val userRequest = UserInfoResponse(name, role)

        viewModelScope.launch {
            try {
                val response = ApiManager.getWebService().createUser(userRequest)
                if (response.isSuccessful) {
                    createUserStatus.postValue("User created successfully!")
                } else {
                    createUserStatus.postValue("Failed to create user!")
                }
            } catch (e: Exception) {
                createUserStatus.postValue("Error: ${e.message}")
            }
        }
    }
}