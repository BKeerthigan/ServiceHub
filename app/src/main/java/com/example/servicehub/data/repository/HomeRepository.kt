package com.example.servicehub.repository

import com.example.servicehub.data.model.AdItem
import com.example.servicehub.data.model.TypeItem
import com.example.servicehub.data.remote.ApiClient

class HomeRepository {

    private val api = ApiClient.apiService

    suspend fun getTypes(): Result<List<TypeItem>> {
        return try {
            val res = api.getTypeList()
            if (res.isSuccessful) Result.success(res.body()?.data.orEmpty())
            else Result.failure(Exception("TypeList failed: ${res.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAds(): Result<List<AdItem>> {
        return try {
            val res = api.getAdList()
            if (res.isSuccessful) Result.success(res.body()?.data.orEmpty())
            else Result.failure(Exception("AdList failed: ${res.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
