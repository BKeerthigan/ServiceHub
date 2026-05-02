package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ItemDetail
import com.example.servicehub.data.model.ProductItem
import com.example.servicehub.data.repository.FoodRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductDetailsUiState(
    val siblings: List<ProductItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val items: List<ItemDetail> = emptyList(),
    val selectedPiid: String = "",
    val selectedCategory: String? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class ProductDetailsViewModel : ViewModel() {

    private val repo = FoodRepository()

    private val _state = MutableStateFlow(ProductDetailsUiState())
    val state: StateFlow<ProductDetailsUiState> = _state

    fun init(listId: String, piid: String) {
        _state.value = _state.value.copy(selectedPiid = piid)
        loadSiblings(listId)
        loadForPiid(piid)
    }

    private fun loadSiblings(listId: String) {
        viewModelScope.launch {
            try {
                val response = repo.getProductItems(listId)
                _state.value = _state.value.copy(siblings = response.data)
            } catch (_: Exception) { }
        }
    }

    fun loadForPiid(piid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                selectedPiid = piid,
                selectedCategory = null
            )
            try {
                val itemsDeferred = async { repo.getItemList(piid) }
                val categoriesDeferred = async { repo.getCategoryList(piid) }
                val items = itemsDeferred.await()
                val categories = categoriesDeferred.await()
                _state.value = _state.value.copy(
                    loading = false,
                    items = items.data,
                    categories = categories.data.mapNotNull { it.itemCategory },
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun filteredItems(): List<ItemDetail> {
        val cat = _state.value.selectedCategory ?: return _state.value.items
        // Chips return "26Kg", items return "26" — strip "Kg" from both before comparing
        val normalizedCat = cat.replace("kg", "", ignoreCase = true).trim()
        return _state.value.items.filter {
            it.itemCategory?.replace("kg", "", ignoreCase = true)?.trim() == normalizedCat
        }
    }
}
