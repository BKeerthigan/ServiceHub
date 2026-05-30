package com.example.servicehub.data.model

data class YourDetailsItem(
    val company_name: String? = null,
    val contact_name: String? = null,
    val mobile_app: String? = null,
    val address: String? = null,
    val landmark: String? = null,
    val city: String? = null,
    val pincode: String? = null
)

data class YourDetailsResponse(
    val data: List<YourDetailsItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class EditNameResponse(
    val data: List<EditNameData>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class EditNameData(
    val action: String? = null
)

data class EditAddressResponse(
    val data: List<EditAddressData>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class EditAddressData(
    val company_name: String? = null
)
