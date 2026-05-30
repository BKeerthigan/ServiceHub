package com.example.servicehub.data.repository

import com.example.servicehub.data.model.SchemeItem
import com.example.servicehub.data.model.TargetSchemeInfo
import com.example.servicehub.data.remote.ApiClient

class TargetSchemeRepository {
    private val api = ApiClient.apiService

    suspend fun getTargetSchemeInfo(): TargetSchemeInfo? =
        api.getTargetSchemeInfo().data?.firstOrNull()

    suspend fun getActiveSchemes(companyId: String): List<SchemeItem> =
        try { api.getActiveSchemes(companyId).data ?: emptyList() }
        catch (_: Exception) { emptyList() }

    suspend fun getPastSchemes(companyId: String): List<SchemeItem> =
        try { api.getPastSchemes(companyId).data ?: emptyList() }
        catch (_: Exception) { emptyList() }
}
