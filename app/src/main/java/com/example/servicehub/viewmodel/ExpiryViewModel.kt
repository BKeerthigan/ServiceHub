package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ExpiryItem
import com.example.servicehub.data.model.ExpiryReturnedItem
import com.example.servicehub.data.repository.ExpiryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpiryUiState(
    val items: List<ExpiryItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val actionResult: String? = null,
    val actionError: String? = null
)

data class ExpiryReturnedUiState(
    val items: List<ExpiryReturnedItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class ExpiryViewModel : ViewModel() {
    private val repo = ExpiryRepository()

    private val _state = MutableStateFlow(ExpiryUiState())
    val state: StateFlow<ExpiryUiState> = _state

    private val _returnedState = MutableStateFlow(ExpiryReturnedUiState())
    val returnedState: StateFlow<ExpiryReturnedUiState> = _returnedState

    fun load(companyId: String) {
        viewModelScope.launch {
            _state.value = ExpiryUiState(loading = true)
            try {
                val items = repo.getExpiryList(companyId)
                _state.value = ExpiryUiState(items = items, loading = false)
            } catch (e: Exception) {
                _state.value = ExpiryUiState(loading = false, error = e.message)
            }
        }
    }

    fun loadReturned(companyId: String) {
        viewModelScope.launch {
            _returnedState.value = ExpiryReturnedUiState(loading = true)
            try {
                val items = repo.getExpiryReturned(companyId)
                _returnedState.value = ExpiryReturnedUiState(items = items, loading = false)
            } catch (e: Exception) {
                _returnedState.value = ExpiryReturnedUiState(loading = false, error = e.message)
            }
        }
    }

    fun submitExpiryReturn(
        salesOrderId: String,
        companyId: String,
        itemId: String,
        mfgDate: String,
        expDate: String,
        quantity: Int
    ) {
        viewModelScope.launch {
            try {
                val result = repo.addExpiryReturn(salesOrderId, companyId, itemId, mfgDate, expDate, quantity)
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
