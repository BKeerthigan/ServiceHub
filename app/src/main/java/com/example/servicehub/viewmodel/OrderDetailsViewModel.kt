package com.example.servicehub.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.OrderDetailsData
import com.example.servicehub.data.repository.OrderDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val loading: Boolean = false,
    val data: OrderDetailsData? = null,
    val error: String? = null
)

class OrderDetailsViewModel : ViewModel() {
    private val repo = OrderDetailsRepository()
    private val _state = MutableStateFlow(OrderDetailsUiState())
    val state: StateFlow<OrderDetailsUiState> = _state

    fun load(salesOrderId: String) {
        Log.d("OrderDetails", "sales_order_id = $salesOrderId  →  https://jmsn.in/mapi/sapiorderdetails.php?sales_order_id=$salesOrderId")
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val data = repo.getOrderDetails(salesOrderId)
                _state.update { it.copy(loading = false, data = data) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
