package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class AccountData(
    val company_name: String? = null,
    val mobile_app: String? = null,
    val contact_name: String? = null,
    @SerializedName("Deliveries") val deliveries: String? = null,
    @SerializedName("Returns") val returns: String? = null,
    @SerializedName("Target Schemes") val targetSchemes: String? = null,
    @SerializedName("Expiry Support") val expirySupport: String? = null,
    @SerializedName("Account Settings") val accountSettings: String? = null,
    @SerializedName("Help_Message") val helpMessage: String? = null,
    @SerializedName("Help_Number") val helpNumber: String? = null
)

data class AccountListResponse(
    val data: List<AccountData>? = null,
    val success: Int? = null,
    val message: String? = null
)
