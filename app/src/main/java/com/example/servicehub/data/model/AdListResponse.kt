package com.example.servicehub.data.model

data class AdListResponse(
    val data: List<AdItem>?,
    val success: Int?,
    val message: String?
)

data class AdItem(
    val name: String?,
    val imgsrc: String?
)
