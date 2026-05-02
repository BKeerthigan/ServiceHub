package com.example.servicehub.data.model

data class LoginData(
    val check_flag: String? = null,
    // API may return company_id as int or string — capture both common names
    val company_id: Any? = null,
    val cid: Any? = null,
    val id: Any? = null,
    val profile_id: Any? = null,
    val company_name: String? = null,
    val contact_name: String? = null,
    val address: String? = null
) {
    // Returns the first non-blank company identifier found
    fun resolvedCompanyId(): String? {
        for (field in listOf(company_id, cid, id, profile_id)) {
            val v = field?.toString()?.trim()
            if (!v.isNullOrBlank() && v != "null") return v
        }
        return null
    }
}