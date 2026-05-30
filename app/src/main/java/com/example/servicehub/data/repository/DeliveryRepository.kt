package com.example.servicehub.data.repository

import android.util.Log
import com.example.servicehub.data.model.CancelDetailData
import com.example.servicehub.data.model.CancelledOrderItem
import com.example.servicehub.data.model.DeliveryItem
import com.example.servicehub.data.model.toDeliveryItem
import com.example.servicehub.data.remote.ApiClient

class DeliveryRepository {
    private val api = ApiClient.apiService

    suspend fun getDeliveries(companyId: String): List<DeliveryItem> {
        Log.d("DELIVERY", "Fetching deliveries for company_id=$companyId")
        val response = api.getDeliveryList(companyId)
        Log.d("DELIVERY", "success=${response.success} message=${response.message} dataSize=${response.data?.size}")
        response.data?.forEachIndexed { i, item ->
            Log.d("DELIVERY", "item[$i] del_message=${item.del_message} order=${item.sales_order_number} id=${item.sales_order_id}")
        }
        val items = response.data?.map { it.toDeliveryItem() } ?: emptyList()
        Log.d("DELIVERY", "parsed ${items.size} items — active=${items.count { !it.delMessage.equals("cancel", true) }} cancelled=${items.count { it.delMessage.equals("cancel", true) }}")
        return items
    }

    suspend fun getCancelledOrders(companyId: String): List<CancelledOrderItem> {
        Log.d("DELIVERY", "Fetching cancelled orders for company_id=$companyId")
        val response = api.getCancelledList(companyId)
        Log.d("DELIVERY", "cancelled success=${response.success} dataSize=${response.data?.size}")
        return response.data ?: emptyList()
    }

    suspend fun getCancelDetail(salesOrderId: String): CancelDetailData? {
        return api.getCancelDetails(salesOrderId).data?.firstOrNull()
    }

    suspend fun cancelOrReorder(salesOrderId: String, reason: String): String {
        val response = api.cancelOrReorder(salesOrderId, reason)
        return response.data?.firstOrNull()?.action.orEmpty()
    }
}
