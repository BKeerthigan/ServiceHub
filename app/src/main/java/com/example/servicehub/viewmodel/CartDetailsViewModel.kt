package com.example.servicehub.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.data.model.CartDetails
import com.example.servicehub.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartDetailsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val details: CartDetails? = null
)

class CartDetailsViewModel : ViewModel() {

    private val repo = CartRepository()

    private val _state = MutableStateFlow(CartDetailsUiState())
    val state: StateFlow<CartDetailsUiState> = _state

    private var lastCompanyId = ""

    fun load(companyId: String) {
        lastCompanyId = companyId
        viewModelScope.launch {
            Log.d("CartDetails", "Loading cart with company_id='$companyId'")
            if (companyId.isBlank()) {
                _state.value = CartDetailsUiState(error = "Company ID not found. Please log in again.")
                return@launch
            }
            _state.value = CartDetailsUiState(loading = true)
            try {
                val response = repo.getCartDetails(companyId)
                Log.d("CartDetails", "Response: success=${response.success} items=${response.data.size}")
                val details = response.data.firstOrNull()
                details?.parsedItems()?.forEachIndexed { i, item ->
                    Log.d("CartDetails", "ParsedItem[$i] itemId='${item.itemId}' name='${item.name}' qty='${item.quantity}'")
                }
                _state.value = CartDetailsUiState(details = details)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error loading cart", e)
                _state.value = CartDetailsUiState(error = e.message ?: "Failed to load cart")
            }
        }
    }

    fun deleteOne(itemId: String, price: String) {
        viewModelScope.launch {
            Log.d("CartDetails", "deleteOne: itemId='$itemId' price='$price' companyId='$lastCompanyId'")
            if (itemId.isBlank()) {
                Log.e("CartDetails", "deleteOne: itemId is blank — backend may not return item_id fields yet")
                return@launch
            }
            try {
                val resp = repo.removeFromCart(lastCompanyId, itemId, 1, price)
                Log.d("CartDetails", "deleteOne: response success=${resp.success} message=${resp.message}")
                com.example.servicehub.cart.CartManager.removeOne(itemId)
                load(lastCompanyId)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error deleting item", e)
            }
        }
    }
}
