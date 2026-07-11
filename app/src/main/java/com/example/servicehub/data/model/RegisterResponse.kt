package com.example.servicehub.data.model

data class RegisterResponse(
    val data: List<RegisterData> = emptyList(),
    val success: Int? = null,
    val message: String? = null
)

data class RegisterData(
    val company_id: Any? = null,
    val company_name: String? = null,
    val contact_name: String? = null,
    val address: String? = null
) {
    fun resolvedCompanyId(): String? {
        val v = company_id?.toString()?.trim()
        return if (!v.isNullOrBlank() && v != "null") v else null
    }
}