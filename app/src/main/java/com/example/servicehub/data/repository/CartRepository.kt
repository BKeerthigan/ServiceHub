package com.example.servicehub.data.repository

import com.example.servicehub.data.remote.ApiClient

class CartRepository {
    private val api = ApiClient.apiService

    suspend fun getCartDetails(companyId: String) = api.getCartDetails(companyId)

    suspend fun addToCart(companyId: String, itemId: String, quantity: Int, price: String) =
        api.addToCart(companyId, itemId, quantity, price)

    suspend fun removeFromCart(companyId: String, itemId: String, quantity: Int, price: String) =
        api.removeFromCart(companyId, itemId, quantity, price)
}
