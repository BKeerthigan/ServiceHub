package com.example.servicehub.data.repository

import com.example.servicehub.data.model.YourDetailsItem
import com.example.servicehub.data.remote.ApiClient

class AccountSettingsRepository {
    private val api = ApiClient.apiService

    suspend fun getYourDetails(companyId: String): YourDetailsItem? =
        api.getYourDetails(companyId).data?.firstOrNull()

    suspend fun editName(companyId: String, contactName: String): String {
        val response = api.editName(companyId, contactName)
        return response.data?.firstOrNull()?.action.orEmpty()
    }

    suspend fun editAddress(
        companyId: String,
        address: String,
        landmark: String,
        city: String,
        pincode: String,
        latitude: String,
        longitude: String
    ): String {
        val response = api.editAddress(companyId, address, landmark, city, pincode, latitude, longitude)
        return response.data?.firstOrNull()?.company_name.orEmpty()
    }
}
