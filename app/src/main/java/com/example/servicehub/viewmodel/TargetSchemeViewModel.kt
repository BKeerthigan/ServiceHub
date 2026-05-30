package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.SchemeItem
import com.example.servicehub.data.model.TargetSchemeInfo
import com.example.servicehub.data.repository.TargetSchemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TargetSchemeUiState(
    val info: TargetSchemeInfo? = null,
    val activeSchemes: List<SchemeItem> = emptyList(),
    val pastSchemes: List<SchemeItem> = emptyList(),
    val loadingInfo: Boolean = true,
    val loadingActive: Boolean = false,
    val loadingPast: Boolean = false,
    val error: String? = null
)

class TargetSchemeViewModel : ViewModel() {
    private val repo = TargetSchemeRepository()

    private val _state = MutableStateFlow(TargetSchemeUiState())
    val state: StateFlow<TargetSchemeUiState> = _state

    fun load(companyId: String) {
        viewModelScope.launch {
            _state.value = TargetSchemeUiState(loadingInfo = true)
            try {
                val info = repo.getTargetSchemeInfo()
                _state.value = _state.value.copy(info = info, loadingInfo = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingInfo = false, error = e.message)
            }
        }
        loadActiveSchemes(companyId)
        loadPastSchemes(companyId)
    }

    fun loadActiveSchemes(companyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingActive = true)
            val items = repo.getActiveSchemes(companyId)
            _state.value = _state.value.copy(activeSchemes = items, loadingActive = false)
        }
    }

    fun loadPastSchemes(companyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingPast = true)
            val items = repo.getPastSchemes(companyId)
            _state.value = _state.value.copy(pastSchemes = items, loadingPast = false)
        }
    }
}
