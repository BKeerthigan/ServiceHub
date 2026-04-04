package com.example.servicehub.data.model

data class RegisterResponse(
    val data: List<RegisterData> = emptyList(),
    val success: Int? = null,
    val message: String? = null
)

data class RegisterData(
    val company_name: String? = null,
    val contact_name: String? = null,
    val address: String? = null
)