package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.AdItem
import com.example.servicehub.data.model.TypeItem
import com.example.servicehub.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val types: List<TypeItem> = emptyList(),
    val ads: List<AdItem> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repo: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun loadHome() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val typesRes = repo.getTypes()
            val adsRes = repo.getAds()

            val err = buildString {
                if (typesRes.isFailure) append(typesRes.exceptionOrNull()?.message ?: "Types error")
                if (adsRes.isFailure) {
                    if (isNotEmpty()) append(" | ")
                    append(adsRes.exceptionOrNull()?.message ?: "Ads error")
                }
            }.ifBlank { null }

            _state.update {
                it.copy(
                    loading = false,
                    types = typesRes.getOrDefault(emptyList()),
                    ads = adsRes.getOrDefault(emptyList()),
                    error = err
                )
            }
        }
    }
}
