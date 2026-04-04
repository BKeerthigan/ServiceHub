package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        mobile: String,
        contactName: String,
        companyNameInput: String,
        address: String,
        landmark: String,
        city: String,
        pincode: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            try {
                val response = ApiClient.apiService.registerDetails(
                    mobile = mobile,
                    contactName = contactName,
                    companyName = companyNameInput,
                    address = address,
                    landmark = landmark,
                    city = city,
                    pincode = pincode
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val first = body?.data?.firstOrNull()

                    val companyName = first?.company_name
                    val contact = first?.contact_name
                    val userAddress = first?.address

                    if (body?.success == 200 && !companyName.isNullOrBlank()) {
                        _uiState.value = RegisterUiState.Success(
                            companyName = companyName,
                            contactName = contact.orEmpty(),
                            address = userAddress.orEmpty()
                        )
                    } else {
                        _uiState.value =
                            RegisterUiState.Error(body?.message ?: "Registration failed")
                    }

                } else {
                    _uiState.value =
                        RegisterUiState.Error("Server error: ${response.code()}")
                }

            } catch (e: Exception) {
                _uiState.value =
                    RegisterUiState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }
}
