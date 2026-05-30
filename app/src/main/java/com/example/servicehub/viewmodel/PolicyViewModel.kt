package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.PolicyData
import com.example.servicehub.data.repository.PolicyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PolicyUiState(
    val data: PolicyData? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class PolicyViewModel : ViewModel() {
    private val repo = PolicyRepository()

    private val _state = MutableStateFlow(PolicyUiState())
    val state: StateFlow<PolicyUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = PolicyUiState(loading = true)
            try {
                val data = repo.getPolicyInfo()
                _state.value = PolicyUiState(data = data, loading = false)
            } catch (e: Exception) {
                _state.value = PolicyUiState(loading = false, error = e.message)
            }
        }
    }
}
