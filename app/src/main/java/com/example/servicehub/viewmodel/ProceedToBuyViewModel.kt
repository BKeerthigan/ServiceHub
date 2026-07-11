package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.cart.CartManager
import com.example.servicehub.data.model.PlaceOrderResponse
import com.example.servicehub.data.model.ProceedToBuyData
import com.example.servicehub.data.repository.ProceedToBuyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentMode { COD, QR }

enum class PtbLocStatus { IDLE, FETCHING, OBTAINED, PERMISSION_DENIED, SERVICES_OFF, UNAVAILABLE }

data class ProceedToBuyUiState(
    val loading: Boolean = false,
    val checkFlag: String? = null,
    val apiMessage: String? = null,
    val info: ProceedToBuyData? = null,
    val selectedMode: PaymentMode = PaymentMode.COD,
    val locStatus: PtbLocStatus = PtbLocStatus.IDLE,
    val lat: String = "",
    val lng: String = "",
    val placing: Boolean = false,
    val orderSuccess: PlaceOrderResponse? = null,
    val error: String? = null,
    val redirectToRegister: Boolean = false
)

class ProceedToBuyViewModel : ViewModel() {

    private val repo = ProceedToBuyRepository()
    private val _state = MutableStateFlow(ProceedToBuyUiState())
    val state: StateFlow<ProceedToBuyUiState> = _state

    fun load(mobileApp: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val (data, message) = repo.getProceedToBuyInfo(mobileApp)
                if (data?.checkFlag == null) {
                    _state.update { it.copy(loading = false, redirectToRegister = true) }
                } else {
                    // flag="0" or flag="1" — both allowed to pay
                    _state.update { it.copy(loading = false, checkFlag = data.checkFlag, info = data) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun selectMode(mode: PaymentMode) = _state.update { it.copy(selectedMode = mode) }

    fun updateLocation(lat: String, lng: String) =
        _state.update { it.copy(locStatus = PtbLocStatus.OBTAINED, lat = lat, lng = lng) }

    fun setLocStatus(status: PtbLocStatus) = _state.update { it.copy(locStatus = status) }

    fun placeCodOrder(companyId: String) {
        val s = _state.value
        if (s.locStatus != PtbLocStatus.OBTAINED || s.placing) return
        _state.update { it.copy(placing = true, error = null) }   // block immediately
        viewModelScope.launch {
            try {
                val resp = repo.placeCodOrder(companyId, s.lat, s.lng)
                CartManager.clear()
                _state.update { it.copy(placing = false, orderSuccess = resp) }
            } catch (e: Exception) {
                _state.update { it.copy(placing = false, error = e.message ?: "Failed to place order") }
            }
        }
    }

    fun clearSuccess() = _state.update { it.copy(orderSuccess = null) }
    fun clearError()   = _state.update { it.copy(error = null) }
}
