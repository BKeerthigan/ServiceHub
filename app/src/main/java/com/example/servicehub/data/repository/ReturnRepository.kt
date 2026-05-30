package com.example.servicehub.data.repository

import com.example.servicehub.data.model.ReturnItem
import com.example.servicehub.data.model.ReturnedOrderItem
import com.example.servicehub.data.remote.ApiClient

class ReturnRepository {
    private val api = ApiClient.apiService

    suspend fun getReturnItems(companyId: String): List<ReturnItem> =
        api.getReturnList(companyId).data ?: emptyList()

    suspend fun getReturnedOrders(companyId: String): List<ReturnedOrderItem> =
        api.getReturnedOrders(companyId).data ?: emptyList()

    suspend fun createReturn(
        salesOrderId: String,
        companyId: String,
        itemId: String,
        reason: String,
        quantity: Int
    ): String {
        val response = api.createReturn(salesOrderId, companyId, itemId, reason, quantity)
        return response.data?.firstOrNull()?.action.orEmpty()
    }
}
