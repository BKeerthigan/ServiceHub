package com.example.servicehub.data.model

data class TypeListResponse(
    val data: List<TypeItem>?,
    val success: Int?,
    val message: String?
)

data class TypeItem(
    val type_id: String?,
    val name: String?,
    val descripition: String?,
    val imgsrc: String?
)