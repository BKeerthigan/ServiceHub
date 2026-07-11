package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.AccountData
import com.example.servicehub.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val data: AccountData? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class AccountViewModel : ViewModel() {
    private val repo = AccountRepository()

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state

    fun load(companyId: String) {
        if (companyId.isBlank()) {
            _state.value = AccountUiState(loading = false, error = "Please complete registration to view account details.")
            return
        }
        viewModelScope.launch {
            _state.value = AccountUiState(loading = true)
            try {
                val data = repo.getAccount(companyId)
                _state.value = AccountUiState(data = data, loading = false)
            } catch (e: Exception) {
                _state.value = AccountUiState(loading = false, error = e.message)
            }
        }
    }
}
