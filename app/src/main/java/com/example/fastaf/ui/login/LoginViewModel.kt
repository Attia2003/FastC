package com.example.fastaf.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel :ViewModel() {
    val UserName = MutableLiveData<String>("att")
    val ErorUserName = MutableLiveData<String>()

    val event = MutableLiveData<LoginViewEvent>()



    fun GoToHome(){
        event.postValue(LoginViewEvent.NavigateToHome)
    }

}