package com.example.servicehub.data.model

data class LoginResponse(
    val data: List<LoginData> = emptyList(),
    val success: Int? = null,
    val Failed: Int? = null,
    val message: String? = null
)

