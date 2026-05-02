package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.servicehub.data.remote.ApiClient
import com.example.servicehub.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class GoHome(val companyName: String ,val contactName: String,
                      val address: String) : LoginUiState()
    data class GoRegister(val phone: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(mobile: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val response = ApiClient.apiService.checkLoginFlag(mobile)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val first = body.data.firstOrNull()

                    val checkFlag = first?.check_flag  // "1", "0", or null

                    Log.d("LOGIN_RAW", "Full login data object = $first")
                    Log.d("LOGIN_RAW", "success=${body.success} Failed=${body.Failed} message=${body.message}")

                    // Store company_id for cart API (fallback to phone number)
                    UserSession.phone = mobile
                    val resolvedId = first?.resolvedCompanyId()
                    UserSession.companyId = if (!resolvedId.isNullOrBlank()) resolvedId else mobile
                    Log.d("LOGIN_RAW", "company_id resolved = '${UserSession.companyId}'")

                    if (checkFlag == null) {
                        _uiState.value = LoginUiState.GoRegister(mobile)
                    } else {
                        // "1" or "0" → Home
                        _uiState.value = LoginUiState.GoHome(
                            companyName = first.company_name.orEmpty(),
                            contactName = first.contact_name.orEmpty(),
                            address = first.address.orEmpty()
                        )
                    }
                } else {
                    _uiState.value = LoginUiState.Error("HTTP ${response.code()}")
                }

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}
