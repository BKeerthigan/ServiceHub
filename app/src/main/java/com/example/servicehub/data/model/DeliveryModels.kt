package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class DeliveryReasonData(
    val heading: String? = null,
    @SerializedName("1reason") val r1: String? = null,
    @SerializedName("2reason") val r2: String? = null,
    @SerializedName("3reason") val r3: String? = null,
    @SerializedName("4reason") val r4: String? = null
)

data class DeliveryItemRaw(
    val del_message: String? = null,
    val sales_order_number: String? = null,
    val order_date: String? = null,
    val net_value: String? = null,
    val sales_order_id: String? = null,
    val payment_status: String? = null,
    val reasons: List<DeliveryReasonData>? = null
)

data class DeliveryItem(
    val salesOrderId: String,
    val salesOrderNumber: String,
    val orderDate: String,
    val netValue: String,
    val delMessage: String,
    val paymentStatus: String,
    val cancelReasons: List<String>
)

fun DeliveryItemRaw.toDeliveryItem(): DeliveryItem {
    val isPending = del_message.equals("pending", ignoreCase = true)
    val reasonObj = reasons?.firstOrNull()
    val cancelReasons = if (isPending)
        listOfNotNull(reasonObj?.r2, reasonObj?.r3, reasonObj?.r4)
            .filter { it.isNotBlank() } + listOf("Others")
    else emptyList()

    return DeliveryItem(
        salesOrderId     = sales_order_id.orEmpty(),
        salesOrderNumber = sales_order_number.orEmpty(),
        orderDate        = order_date.orEmpty(),
        netValue         = net_value.orEmpty(),
        delMessage       = del_message.orEmpty(),
        paymentStatus    = payment_status.orEmpty(),
        cancelReasons    = cancelReasons
    )
}

data class DeliveryListResponse(
    val data: List<DeliveryItemRaw>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CancelledOrderItem(
    val del_message: String? = null,
    val sales_order_number: String? = null,
    val order_date: String? = null,
    val net_value: String? = null,
    val sales_order_id: String? = null,
    val status: String? = null,
    val reason: String? = null
)

data class CancelledListResponse(
    val data: List<CancelledOrderItem>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CancelDetailItemApi(
    @SerializedName("item_id")       val itemId: String? = null,
    @SerializedName("item_name")     val itemName: String? = null,
    @SerializedName("item_category") val itemCategory: String? = null,
    @SerializedName("price")         val price: String? = null,
    @SerializedName("quantity")      val quantity: String? = null,
    @SerializedName("imgsrc")        val imgSrc: String? = null,
)

data class CancelDetailItem(
    val itemId: String,
    val itemName: String,
    val itemCategory: String,
    val price: String,
    val quantity: String,
    val imgSrc: String
)

data class CancelDetailData(
    val del_message: String? = null,
    val sales_order_number: String? = null,
    val order_date: String? = null,
    val net_value: String? = null,
    val sales_order_id: String? = null,
    val status: String? = null,
    val shipping: String? = null,
    val shipping_charge: String? = null,
    val address: String? = null,
    @SerializedName("item") val items: List<CancelDetailItemApi> = emptyList(),
) {
    fun parsedItems(): List<CancelDetailItem> = items
        .filter { !it.itemId.isNullOrBlank() }
        .map { api ->
            CancelDetailItem(
                itemId       = api.itemId.orEmpty(),
                itemName     = api.itemName.orEmpty(),
                itemCategory = api.itemCategory.orEmpty(),
                price        = api.price.orEmpty(),
                quantity     = api.quantity.orEmpty(),
                imgSrc       = api.imgSrc.orEmpty()
            )
        }
}

data class CancelDetailResponse(
    val data: List<CancelDetailData>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CancelReorderResponse(
    val data: List<CancelReorderData>? = null,
    val success: Int? = null,
    val message: String? = null
)

data class CancelReorderData(
    val action: String? = null
)
