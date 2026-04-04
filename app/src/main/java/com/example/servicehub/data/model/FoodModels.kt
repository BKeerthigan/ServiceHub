package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class ProductDetailsResponse(
    @SerializedName("data") val data: List<ProductCategory> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class ProductCategory(
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("type_id") val typeId: String? = null,
    @SerializedName("name") val name: String? = null
)

data class ProductListResponse(
    @SerializedName("data") val data: List<ProductListItem> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class ProductListItem(
    @SerializedName("list_id") val listId: String? = null,
    @SerializedName("typeid") val typeId: String? = null,
    @SerializedName("productid") val productId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("imgsrc") val imgsrc: String? = null
)
data class ProductItemResponse(
    @SerializedName("data") val data: List<ProductItem> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class ProductItem(
    @SerializedName("pid") val pid: String? = null,
    @SerializedName("typeid") val typeId: String? = null,
    @SerializedName("productid") val productId: String? = null,
    @SerializedName("listid") val listId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("nutrition") val nutrition: String? = null,
    @SerializedName("imgsrc") val imgsrc: String? = null
)