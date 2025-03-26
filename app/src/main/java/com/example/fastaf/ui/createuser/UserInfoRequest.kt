package com.example.fastaf.ui.createuser


data class UserInfoRequest(
    val username: String,
    val role: String = "ADMIN"
)