package com.example.servicehub.data.model

data class ExpiryItem(
    val del_message: String? = null,
    val sales_order_number: String? = null,
    val order_date: String? = null,
    val sales_order_id: String? = null,
    val payment_status: String? = null,
    val item_id: String? = null,
    val item_name: String? = null,
    val item_category: String? = null,
    val item_price: String? = null,
    val quantity: String? = null,
    val imgsrc: String? = null,
    val return_status: String? = null
)

data class ExpiryListResponse(
    val data: List<ExpiryItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class ExpiryReturnedItem(
    val sales_order_number: String? = null,
    val return_date: String? = null,
    val amount: String? = null,
    val sales_order_id: String? = null,
    val quantity: String? = null,
    val payment_status: String? = null,
    val return_status: String? = null,
    val mfg_date: String? = null,
    val exp_date: String? = null,
    val item_id: String? = null,
    val item_name: String? = null,
    val item_category: String? = null,
    val imgsrc: String? = null
)

data class ExpiryReturnedResponse(
    val data: List<ExpiryReturnedItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class AddExpiryReturnData(
    val action: String? = null
)

data class AddExpiryReturnResponse(
    val data: List<AddExpiryReturnData>? = null,
    val success: Int? = null,
    val message: String? = null
)
