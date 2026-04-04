package com.example.servicehub.viewmodel

sealed class RegisterUiState {

    object Idle : RegisterUiState()

    object Loading : RegisterUiState()

    data class Success(
        val companyName: String,
        val contactName: String,
        val address: String
    ) : RegisterUiState()

    data class Error(val message: String) : RegisterUiState()
}
