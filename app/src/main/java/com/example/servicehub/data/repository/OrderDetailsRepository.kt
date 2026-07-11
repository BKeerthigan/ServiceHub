package com.example.servicehub.data.repository

import com.example.servicehub.data.model.OrderDetailsData
import com.example.servicehub.data.remote.ApiClient

class OrderDetailsRepository {
    private val api = ApiClient.apiService

    suspend fun getOrderDetails(salesOrderId: String): OrderDetailsData? =
        api.getOrderDetails(salesOrderId).data?.firstOrNull()
}
