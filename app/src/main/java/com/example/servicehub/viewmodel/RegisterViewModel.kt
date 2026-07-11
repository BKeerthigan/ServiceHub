package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.remote.ApiClient
import com.example.servicehub.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _locStatus = MutableStateFlow(PtbLocStatus.IDLE)
    val locStatus: StateFlow<PtbLocStatus> = _locStatus

    private var _lat = ""
    private var _lng = ""

    fun updateLocation(lat: String, lng: String) {
        _lat = lat
        _lng = lng
        _locStatus.value = PtbLocStatus.OBTAINED
    }

    fun setLocStatus(status: PtbLocStatus) {
        _locStatus.value = status
    }

    fun register(
        mobile: String,
        contactName: String,
        companyNameInput: String,
        address: String,
        landmark: String,
        city: String,
        pincode: String,
        salesLead: Int,
        salesPerson: Int
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
                    pincode = pincode,
                    latitude = _lat,
                    longitude = _lng,
                    salesLead = salesLead,
                    salesPerson = salesPerson
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val first = body?.data?.firstOrNull()

                    val companyName = first?.company_name
                    val contact = first?.contact_name
                    val userAddress = first?.address

                    if (body?.success == 200 && !companyName.isNullOrBlank()) {
                        // Registration API may not return company_id — fetch it via login API
                        val resolvedId = first?.resolvedCompanyId()
                        if (!resolvedId.isNullOrBlank()) {
                            UserSession.companyId = resolvedId
                        } else {
                            // Re-call checkLoginFlag to reliably get company_id
                            runCatching {
                                val loginResp = ApiClient.apiService.checkLoginFlag(mobile)
                                if (loginResp.isSuccessful) {
                                    val loginId = loginResp.body()?.data?.firstOrNull()?.resolvedCompanyId()
                                    if (!loginId.isNullOrBlank()) {
                                        UserSession.companyId = loginId
                                    }
                                }
                            }
                        }
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
