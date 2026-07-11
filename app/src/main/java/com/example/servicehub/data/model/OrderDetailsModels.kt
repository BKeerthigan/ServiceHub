package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class OrderDetailsResponse(
    @SerializedName("data")    val data: List<OrderDetailsData>?,
    @SerializedName("success") val success: Int?,
    @SerializedName("message") val message: String?
)

data class OrderDetailsItemApi(
    @SerializedName("item_id")       val itemId: String? = null,
    @SerializedName("item_name")     val itemName: String? = null,
    @SerializedName("item_category") val itemCategory: String? = null,
    @SerializedName("price")         val price: String? = null,
    @SerializedName("quantity")      val quantity: String? = null,
    @SerializedName("imgsrc")        val imgsrc: String? = null,
)

data class OrderDetailsData(
    @SerializedName("call_us")           val callUs: String?,
    @SerializedName("ordered_on")        val orderedOn: String?,
    @SerializedName("sales_order_number")val orderId: String?,
    @SerializedName("net_value")         val netValue: String?,
    @SerializedName("pay_info")          val payInfo: String?,
    @SerializedName("pay_notes1")        val payNotes1: String?,
    @SerializedName("pay_notes2")        val payNotes2: String?,
    @SerializedName("address")           val address: String?,
    @SerializedName("delivery_date")     val deliveryDate: String?,
    @SerializedName("delivery_status")   val deliveryStatus: String?,
    @SerializedName("shipping")          val shipping: String?,
    @SerializedName("shipping_charge")   val shippingCharge: String?,
    @SerializedName("item")              val items: List<OrderDetailsItemApi> = emptyList(),
)
