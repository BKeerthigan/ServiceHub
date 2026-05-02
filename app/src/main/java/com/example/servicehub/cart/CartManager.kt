package com.example.servicehub.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CartEntry(
    val itemId: String,
    val name: String,
    val price: String,
    val quantity: Int
)

object CartManager {

    private val _entries = MutableStateFlow<Map<String, CartEntry>>(emptyMap())
    val entries: StateFlow<Map<String, CartEntry>> = _entries.asStateFlow()

    fun getQuantity(itemId: String): Int = _entries.value[itemId]?.quantity ?: 0

    fun addOne(itemId: String, name: String, price: String) {
        val map = _entries.value.toMutableMap()
        val ex = map[itemId]
        map[itemId] = ex?.copy(quantity = ex.quantity + 1)
            ?: CartEntry(itemId, name, price, 1)
        _entries.value = map
    }

    fun removeOne(itemId: String) {
        val map = _entries.value.toMutableMap()
        val ex = map[itemId] ?: return
        if (ex.quantity <= 1) map.remove(itemId)
        else map[itemId] = ex.copy(quantity = ex.quantity - 1)
        _entries.value = map
    }

    fun totalItems(): Int = _entries.value.values.sumOf { it.quantity }

    fun totalPrice(): Double =
        _entries.value.values.sumOf { (it.price.toDoubleOrNull() ?: 0.0) * it.quantity }
}
