package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ReturnItem
import com.example.servicehub.data.model.ReturnedOrderItem
import com.example.servicehub.data.repository.ReturnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReturnUiState(
    val activeItems: List<ReturnItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val actionResult: String? = null,
    val actionError: String? = null
)

data class ReturnedOrdersUiState(
    val items: List<ReturnedOrderItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class ReturnViewModel : ViewModel() {
    private val repo = ReturnRepository()

    private val _state = MutableStateFlow(ReturnUiState())
    val state: StateFlow<ReturnUiState> = _state

    private val _returnedState = MutableStateFlow(ReturnedOrdersUiState())
    val returnedState: StateFlow<ReturnedOrdersUiState> = _returnedState

    fun load(companyId: String) {
        viewModelScope.launch {
            _state.value = ReturnUiState(loading = true)
            try {
                val all = repo.getReturnItems(companyId)
                _state.value = ReturnUiState(
                    activeItems = all.filter { !it.return_status.equals("Returned", ignoreCase = true) },
                    loading     = false
                )
            } catch (e: Exception) {
                _state.value = ReturnUiState(loading = false, error = e.message)
            }
        }
    }

    fun loadReturnedOrders(companyId: String) {
        viewModelScope.launch {
            _returnedState.value = ReturnedOrdersUiState(loading = true)
            try {
                val items = repo.getReturnedOrders(companyId)
                _returnedState.value = ReturnedOrdersUiState(items = items, loading = false)
            } catch (e: Exception) {
                _returnedState.value = ReturnedOrdersUiState(loading = false, error = e.message)
            }
        }
    }

    fun submitReturn(
        salesOrderId: String,
        companyId: String,
        itemId: String,
        reason: String,
        quantity: Int
    ) {
        viewModelScope.launch {
            try {
                val result = repo.createReturn(salesOrderId, companyId, itemId, reason, quantity)
                _state.value = _state.value.copy(actionResult = result, actionError = null)
                load(companyId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(actionError = e.message)
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(actionResult = null, actionError = null)
    }
}
