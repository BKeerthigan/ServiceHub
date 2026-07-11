package com.example.servicehub.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.cart.CartManager
import com.example.servicehub.data.model.PlaceOrderResponse
import com.example.servicehub.data.model.QrCodeData
import com.example.servicehub.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun todayDate(): String {
    val cal = java.util.Calendar.getInstance()
    val y = cal.get(java.util.Calendar.YEAR)
    val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$y-$m-$d"
}

data class QrPaymentUiState(
    val loadingQr: Boolean = false,
    val qrData: QrCodeData? = null,
    val transRef: String = "",
    val transDate: String = todayDate(),
    val locStatus: PtbLocStatus = PtbLocStatus.IDLE,
    val lat: String = "",
    val lng: String = "",
    val placing: Boolean = false,
    val orderSuccess: PlaceOrderResponse? = null,
    val error: String? = null
)

class QrPaymentViewModel : ViewModel() {

    private val api = ApiClient.apiService
    private val _state = MutableStateFlow(QrPaymentUiState())
    val state: StateFlow<QrPaymentUiState> = _state

    fun loadQrCode() {
        Log.d("QrPayment", "loadQrCode() called")
        viewModelScope.launch {
            _state.update { it.copy(loadingQr = true) }
            runCatching {
                Log.d("QrPayment", "Calling sapiqrcode.php...")
                val response = api.getQrCode()
                Log.d("QrPayment", "Response: success=${response.success} data=${response.data}")
                val data = response.data?.firstOrNull()
                Log.d("QrPayment", "QrData: address=${data?.address} img=${data?.qrcodeImg}")
                _state.update { it.copy(loadingQr = false, qrData = data) }
            }.onFailure { e ->
                Log.e("QrPayment", "loadQrCode FAILED: ${e.message}", e)
                _state.update { it.copy(loadingQr = false, error = e.message) }
            }
        }
    }

    fun setTransRef(v: String)  = _state.update { it.copy(transRef = v) }
    fun setTransDate(v: String) = _state.update { it.copy(transDate = v) }

    fun updateLocation(lat: String, lng: String) =
        _state.update { it.copy(locStatus = PtbLocStatus.OBTAINED, lat = lat, lng = lng) }

    fun setLocStatus(status: PtbLocStatus) = _state.update { it.copy(locStatus = status) }

    fun placeOrder(companyId: String) {
        val s = _state.value
        if (s.locStatus != PtbLocStatus.OBTAINED || s.placing) return
        _state.update { it.copy(placing = true, error = null) }   // block immediately
        viewModelScope.launch {
            runCatching {
                val resp = api.placeQrOrder(companyId, s.lat, s.lng, s.transRef, s.transDate)
                CartManager.clear()
                _state.update { it.copy(placing = false, orderSuccess = resp) }
            }.onFailure { e ->
                _state.update { it.copy(placing = false, error = e.message ?: "Failed to place order") }
            }
        }
    }

    fun clearSuccess() = _state.update { it.copy(orderSuccess = null) }
    fun clearError()   = _state.update { it.copy(error = null) }
}
