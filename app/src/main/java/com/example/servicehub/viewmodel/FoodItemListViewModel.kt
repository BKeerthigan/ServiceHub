package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ProductItem
import com.example.servicehub.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FoodItemUiState(
    val loading: Boolean = false,
    val items: List<ProductItem> = emptyList(),
    val error: String? = null
)

class FoodItemListViewModel : ViewModel() {

    private val repo = FoodRepository()

    private val _state = MutableStateFlow(FoodItemUiState())
    val state: StateFlow<FoodItemUiState> = _state

    fun loadItems(listId: String) {
        viewModelScope.launch {
            try {
                _state.value = FoodItemUiState(loading = true)

                val response = repo.getProductItems(listId)

                _state.value = FoodItemUiState(
                    loading = false,
                    items = response.data,
                    error = null
                )

            } catch (e: Exception) {
                _state.value = FoodItemUiState(
                    loading = false,
                    items = emptyList(),
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }
}