package com.example.fastaf.eror


import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:Binder")
fun  BindingErorTextInputLayout(
    textInputLayout: TextInputLayout,
    messageEror:String?){
    textInputLayout.error = messageEror?:""

}