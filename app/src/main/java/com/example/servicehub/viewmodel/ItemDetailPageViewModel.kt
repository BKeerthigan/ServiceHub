package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ItemDetail
import com.example.servicehub.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ItemDetailPageUiState(
    val item: ItemDetail? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class ItemDetailPageViewModel : ViewModel() {

    private val repo = FoodRepository()

    private val _state = MutableStateFlow(ItemDetailPageUiState())
    val state: StateFlow<ItemDetailPageUiState> = _state

    fun load(itemId: String) {
        viewModelScope.launch {
            _state.value = ItemDetailPageUiState(loading = true)
            try {
                val response = repo.getItemById(itemId)
                val raw = response.data.firstOrNull()
                // sapiitembyid.php may not echo item_id back in the response body;
                // fall back to the id we queried with so cart sync always has a valid id.
                val item = if (raw != null && raw.itemId.isNullOrBlank()) raw.copy(itemId = itemId) else raw
                _state.value = ItemDetailPageUiState(loading = false, item = item)
            } catch (e: Exception) {
                _state.value = ItemDetailPageUiState(
                    loading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }
}
