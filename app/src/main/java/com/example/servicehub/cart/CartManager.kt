package com.example.servicehub.cart

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    fun getLastPhone(): String = prefs?.getString("last_phone", "") ?: ""

    fun saveLastPhone(phone: String) {
        prefs?.edit()?.putString("last_phone", phone)?.apply()
    }

    private fun loadFromPrefs() {
        val json = prefs?.getString("cart_entries", null) ?: return
        val type = object : TypeToken<Map<String, CartEntry>>() {}.type
        val saved = runCatching {
            gson.fromJson<Map<String, CartEntry>>(json, type)
        }.getOrNull()
        if (!saved.isNullOrEmpty()) _entries.value = saved
    }

    private fun saveToPrefs() {
        prefs?.edit()?.putString("cart_entries", gson.toJson(_entries.value))?.apply()
    }

    fun getQuantity(itemId: String): Int = _entries.value[itemId]?.quantity ?: 0

    fun addOne(itemId: String, name: String, price: String) {
        val map = _entries.value.toMutableMap()
        val ex = map[itemId]
        map[itemId] = ex?.copy(quantity = ex.quantity + 1) ?: CartEntry(itemId, name, price, 1)
        _entries.value = map
        saveToPrefs()
    }

    fun removeOne(itemId: String) {
        val map = _entries.value.toMutableMap()
        val ex = map[itemId] ?: return
        if (ex.quantity <= 1) map.remove(itemId) else map[itemId] = ex.copy(quantity = ex.quantity - 1)
        _entries.value = map
        saveToPrefs()
    }

    fun totalItems(): Int = _entries.value.values.sumOf { it.quantity }

    fun totalPrice(): Double =
        _entries.value.values.sumOf { (it.price.toDoubleOrNull() ?: 0.0) * it.quantity }

    fun removeItem(itemId: String) {
        val map = _entries.value.toMutableMap()
        map.remove(itemId)
        _entries.value = map
        saveToPrefs()
    }

    fun clear() {
        _entries.value = emptyMap()
        prefs?.edit()?.remove("cart_entries")?.apply()
    }
}
