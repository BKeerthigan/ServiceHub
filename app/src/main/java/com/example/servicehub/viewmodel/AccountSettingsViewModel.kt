package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.YourDetailsItem
import com.example.servicehub.data.repository.AccountSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class YourDetailsUiState(
    val details: YourDetailsItem? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val saveResult: String? = null,
    val saveError: String? = null
)

class AccountSettingsViewModel : ViewModel() {
    private val repo = AccountSettingsRepository()

    private val _state = MutableStateFlow(YourDetailsUiState())
    val state: StateFlow<YourDetailsUiState> = _state

    fun load(companyId: String) {
        viewModelScope.launch {
            _state.value = YourDetailsUiState(loading = true)
            try {
                val details = repo.getYourDetails(companyId)
                _state.value = YourDetailsUiState(details = details, loading = false)
            } catch (e: Exception) {
                _state.value = YourDetailsUiState(loading = false, error = e.message)
            }
        }
    }

    fun editName(companyId: String, name: String) {
        viewModelScope.launch {
            try {
                val result = repo.editName(companyId, name)
                _state.value = _state.value.copy(
                    saveResult = result.ifBlank { "Name updated successfully" },
                    saveError = null,
                    details = _state.value.details?.copy(contact_name = name)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(saveError = e.message)
            }
        }
    }

    fun editAddress(
        companyId: String,
        address: String,
        landmark: String,
        city: String,
        pincode: String,
        latitude: String,
        longitude: String
    ) {
        viewModelScope.launch {
            try {
                repo.editAddress(companyId, address, landmark, city, pincode, latitude, longitude)
                _state.value = _state.value.copy(
                    saveResult = "Address updated successfully",
                    saveError  = null,
                    details    = _state.value.details?.copy(
                        address  = address,
                        landmark = landmark,
                        city     = city,
                        pincode  = pincode
                    )
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(saveError = e.message)
            }
        }
    }

    fun clearSaveState() {
        _state.value = _state.value.copy(saveResult = null, saveError = null)
    }
}
