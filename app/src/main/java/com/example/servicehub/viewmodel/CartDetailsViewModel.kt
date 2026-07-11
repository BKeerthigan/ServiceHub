package com.example.servicehub.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicehub.cart.CartManager
import com.example.servicehub.data.model.CartDetails
import com.example.servicehub.data.model.CartItemApi
import com.example.servicehub.data.model.YourDetailsItem
import com.example.servicehub.data.remote.ApiClient
import com.example.servicehub.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartDetailsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val details: CartDetails? = null,
    val addressDetails: YourDetailsItem? = null
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
                // No companyId (guest/unregistered) — show local cart without API
                val localItems = CartManager.entries.value.values.map { entry ->
                    CartItemApi(
                        itemId       = entry.itemId,
                        itemName     = entry.name,
                        itemCategory = "",
                        price        = entry.price,
                        quantity     = entry.quantity.toString(),
                        imgsrc       = null
                    )
                }
                _state.value = CartDetailsUiState(
                    details = CartDetails(items = localItems)
                )
                return@launch
            }
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val response = repo.getCartDetails(companyId)
                Log.d("CartDetails", "Response: success=${response.success} items=${response.data.size}")
                val details = response.data.firstOrNull()

                // Always use CartManager as source of truth for items
                // API is only used for address / shipping / policy info
                val localItems = CartManager.entries.value.values.map { entry ->
                    CartItemApi(
                        itemId       = entry.itemId,
                        itemName     = entry.name,
                        itemCategory = "",
                        price        = entry.price,
                        quantity     = entry.quantity.toString(),
                        imgsrc       = null
                    )
                }

                Log.d("CartDetails", "CartManager has ${localItems.size} items, API has ${details?.items?.size ?: 0} items")

                val finalDetails = when {
                    localItems.isNotEmpty() && details != null ->
                        details.copy(items = localItems)   // use CartManager items, keep API metadata
                    localItems.isNotEmpty() ->
                        com.example.servicehub.data.model.CartDetails(items = localItems)
                    else ->
                        details   // CartManager empty → show whatever API returned
                }

                _state.value = _state.value.copy(loading = false, details = finalDetails, error = null)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error loading cart", e)
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Failed to load cart")
            }
        }
    }

    private fun refreshItems() {
        val current = _state.value.details ?: return
        val localItems = CartManager.entries.value.values.map { entry ->
            CartItemApi(
                itemId       = entry.itemId,
                itemName     = entry.name,
                itemCategory = "",
                price        = entry.price,
                quantity     = entry.quantity.toString(),
                imgsrc       = null
            )
        }
        val updated = current.copy(items = localItems)
        _state.value = _state.value.copy(details = updated)
    }

    fun deleteOne(itemId: String, price: String) {
        if (itemId.isBlank()) return
        CartManager.removeOne(itemId)
        refreshItems()
        viewModelScope.launch {
            try {
                repo.removeFromCart(lastCompanyId, itemId, 1, price)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error syncing remove to server", e)
            }
        }
    }

    fun addOne(itemId: String) {
        val entry = CartManager.entries.value[itemId] ?: return
        CartManager.addOne(itemId, entry.name, entry.price)
        refreshItems()
        viewModelScope.launch {
            try {
                repo.addToCart(lastCompanyId, itemId, 1, entry.price)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error syncing add to server", e)
            }
        }
    }

    fun fetchAddressDetails(companyId: String) {
        if (companyId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val resp = ApiClient.apiService.getYourDetails(companyId)
                val item = resp.data?.firstOrNull()
                if (item != null) {
                    _state.value = _state.value.copy(addressDetails = item)
                }
            }
        }
    }

    fun deleteItem(itemId: String, price: String) {
        val entry = CartManager.entries.value[itemId] ?: return
        val qty = entry.quantity
        CartManager.removeItem(itemId)
        refreshItems()
        viewModelScope.launch {
            try {
                repo.removeFromCart(lastCompanyId, itemId, qty, price)
            } catch (e: Exception) {
                Log.e("CartDetails", "Error syncing delete to server", e)
            }
        }
    }
}
