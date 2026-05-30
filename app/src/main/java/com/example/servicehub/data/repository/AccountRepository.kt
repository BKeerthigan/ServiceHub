package com.example.servicehub.data.repository

import com.example.servicehub.data.model.AccountData
import com.example.servicehub.data.remote.ApiClient

class AccountRepository {
    private val api = ApiClient.apiService

    suspend fun getAccount(companyId: String): AccountData? {
        val response = api.getAccountList(companyId)
        return response.data?.firstOrNull()
    }
}
