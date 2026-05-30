package com.example.servicehub.data.model

data class ReturnItem(
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
    val return_status: String? = null   // "Create Return" | "On Progress" | "Returned"
)

data class ReturnListResponse(
    val data: List<ReturnItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class ReturnedOrderItem(
    val sales_order_number: String? = null,
    val return_date: String? = null,
    val amount: String? = null,
    val sales_order_id: String? = null,
    val quantity: String? = null,
    val payment_status: String? = null,
    val return_status: String? = null,
    val reason: String? = null,
    val item_id: String? = null,
    val item_name: String? = null,
    val item_category: String? = null
)

data class ReturnedOrdersResponse(
    val data: List<ReturnedOrderItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CreateReturnResponse(
    val data: List<CreateReturnData>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CreateReturnData(
    val action: String? = null
)
