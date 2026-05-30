package com.example.servicehub.data.repository

import com.example.servicehub.data.model.ExpiryItem
import com.example.servicehub.data.model.ExpiryReturnedItem
import com.example.servicehub.data.remote.ApiClient

class ExpiryRepository {
    private val api = ApiClient.apiService

    suspend fun getExpiryList(companyId: String): List<ExpiryItem> =
        api.getExpiryList(companyId).data ?: emptyList()

    suspend fun getExpiryReturned(companyId: String): List<ExpiryReturnedItem> =
        api.getExpiryReturned(companyId).data ?: emptyList()

    suspend fun addExpiryReturn(
        salesOrderId: String,
        companyId: String,
        itemId: String,
        mfgDate: String,
        expDate: String,
        quantity: Int
    ): String {
        val response = api.addExpiryReturn(salesOrderId, companyId, itemId, mfgDate, expDate, quantity)
        return response.data?.firstOrNull()?.action.orEmpty()
    }
}
