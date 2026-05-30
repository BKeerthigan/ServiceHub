package com.example.servicehub.data.repository

import com.example.servicehub.data.model.PolicyData
import com.example.servicehub.data.remote.ApiClient

class PolicyRepository {
    private val api = ApiClient.apiService

    suspend fun getPolicyInfo(): PolicyData? =
        api.getPolicyInfo().data?.firstOrNull()
}
