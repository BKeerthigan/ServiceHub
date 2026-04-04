package com.example.servicehub.data.repository

import com.example.servicehub.data.remote.ApiClient

class FoodRepository {
    private val api = ApiClient.apiService

    suspend fun getProductDetails() = api.getProductDetails()
    suspend fun getProductList() = api.getProductList()
    suspend fun getProductItems(listId: String) =api.getProductItems(listId)

}
