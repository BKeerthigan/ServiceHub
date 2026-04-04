package com.example.servicehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.ProductCategory
import com.example.servicehub.data.model.ProductListItem
import com.example.servicehub.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FoodSectionUi(
    val productId: String,
    val title: String,
    val items: List<ProductListItem>
)

data class FoodUiState(
    val loading: Boolean = false,
    val sections: List<FoodSectionUi> = emptyList(),
    val error: String? = null
)

class FoodViewModel : ViewModel() {

    private val repo = FoodRepository()

    private val _state = MutableStateFlow(FoodUiState())
    val state: StateFlow<FoodUiState> = _state

    fun loadFood(typeId: String = "1") {
        viewModelScope.launch {
            try {
                _state.value = FoodUiState(loading = true)

                val detailsResp = repo.getProductDetails()
                val listResp = repo.getProductList()

                val categories: List<ProductCategory> =
                    detailsResp.data.filter { it.typeId == typeId }

                val allItems: List<ProductListItem> =
                    listResp.data.filter { it.typeId == typeId }

                val itemsByProductId = allItems.groupBy { it.productId.orEmpty() }

                val sections = categories.map { cat ->
                    val pid = cat.productId.orEmpty()
                    FoodSectionUi(
                        productId = pid,
                        title = cat.name.orEmpty(),
                        items = itemsByProductId[pid].orEmpty()
                    )
                }

                _state.value = FoodUiState(
                    loading = false,
                    sections = sections,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = FoodUiState(
                    loading = false,
                    sections = emptyList(),
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }
}
