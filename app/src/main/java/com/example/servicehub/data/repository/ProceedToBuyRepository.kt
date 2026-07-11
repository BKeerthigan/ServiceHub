package com.example.servicehub.data.repository

import com.example.servicehub.data.model.PlaceOrderResponse
import com.example.servicehub.data.model.ProceedToBuyData
import com.example.servicehub.data.remote.ApiClient

class ProceedToBuyRepository {
    private val api = ApiClient.apiService

    suspend fun getProceedToBuyInfo(mobileApp: String): Pair<ProceedToBuyData?, String?> {
        val resp = api.getProceedToBuyInfo(mobileApp)
        return Pair(resp.data?.firstOrNull(), resp.message)
    }

    suspend fun placeCodOrder(companyId: String, lat: String, lng: String): PlaceOrderResponse =
        api.placeCodOrder(companyId, lat, lng)
}
