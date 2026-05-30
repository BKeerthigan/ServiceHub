package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.CancelledOrderItem
import com.example.servicehub.data.model.DeliveryItem
import com.example.servicehub.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DeliveryUiState(
    val activeItems: List<DeliveryItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val actionResult: String? = null,
    val actionError: String? = null
)

data class CancelledUiState(
    val items: List<CancelledOrderItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class DeliveryViewModel : ViewModel() {
    private val repo = DeliveryRepository()

    private val _state = MutableStateFlow(DeliveryUiState())
    val state: StateFlow<DeliveryUiState> = _state

    private val _cancelledState = MutableStateFlow(CancelledUiState())
    val cancelledState: StateFlow<CancelledUiState> = _cancelledState

    fun load(companyId: String) {
        viewModelScope.launch {
            _state.value = DeliveryUiState(loading = true)
            try {
                val all = repo.getDeliveries(companyId)
                _state.value = DeliveryUiState(
                    activeItems = all.filter { !it.delMessage.equals("cancel", ignoreCase = true) },
                    loading     = false
                )
            } catch (e: Exception) {
                _state.value = DeliveryUiState(loading = false, error = e.message)
            }
        }
    }

    fun loadCancelled(companyId: String) {
        viewModelScope.launch {
            _cancelledState.value = CancelledUiState(loading = true)
            try {
                val items = repo.getCancelledOrders(companyId)
                _cancelledState.value = CancelledUiState(items = items, loading = false)
            } catch (e: Exception) {
                _cancelledState.value = CancelledUiState(loading = false, error = e.message)
            }
        }
    }

    fun submitAction(salesOrderId: String, reason: String, companyId: String) {
        viewModelScope.launch {
            try {
                val result = repo.cancelOrReorder(salesOrderId, reason)
                _state.value = _state.value.copy(actionResult = result, actionError = null)
                load(companyId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(actionError = e.message)
            }
        }
    }

    fun clearActionResult() {
        _state.value = _state.value.copy(actionResult = null, actionError = null)
    }
}
