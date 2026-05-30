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

    @SerializedName("item_id1")       val itemId1: String? = null,
    @SerializedName("item_name1")     val itemName1: String? = null,
    @SerializedName("item_category1") val itemCategory1: String? = null,
    @SerializedName("price1")         val price1: String? = null,
    @SerializedName("quantity1")      val quantity1: String? = null,
    @SerializedName("imgsrc1")        val imgSrc1: String? = null,

    @SerializedName("item_id2")       val itemId2: String? = null,
    @SerializedName("item_name2")     val itemName2: String? = null,
    @SerializedName("item_category2") val itemCategory2: String? = null,
    @SerializedName("price2")         val price2: String? = null,
    @SerializedName("quantity2")      val quantity2: String? = null,
    @SerializedName("imgsrc2")        val imgSrc2: String? = null,

    @SerializedName("item_id3")       val itemId3: String? = null,
    @SerializedName("item_name3")     val itemName3: String? = null,
    @SerializedName("item_category3") val itemCategory3: String? = null,
    @SerializedName("price3")         val price3: String? = null,
    @SerializedName("quantity3")      val quantity3: String? = null,
    @SerializedName("imgsrc3")        val imgSrc3: String? = null,

    @SerializedName("item_id4")       val itemId4: String? = null,
    @SerializedName("item_name4")     val itemName4: String? = null,
    @SerializedName("item_category4") val itemCategory4: String? = null,
    @SerializedName("price4")         val price4: String? = null,
    @SerializedName("quantity4")      val quantity4: String? = null,
    @SerializedName("imgsrc4")        val imgSrc4: String? = null,

    @SerializedName("item_id5")       val itemId5: String? = null,
    @SerializedName("item_name5")     val itemName5: String? = null,
    @SerializedName("item_category5") val itemCategory5: String? = null,
    @SerializedName("price5")         val price5: String? = null,
    @SerializedName("quantity5")      val quantity5: String? = null,
    @SerializedName("imgsrc5")        val imgSrc5: String? = null,

    @SerializedName("item_id6")       val itemId6: String? = null,
    @SerializedName("item_name6")     val itemName6: String? = null,
    @SerializedName("item_category6") val itemCategory6: String? = null,
    @SerializedName("price6")         val price6: String? = null,
    @SerializedName("quantity6")      val quantity6: String? = null,
    @SerializedName("imgsrc6")        val imgSrc6: String? = null,

    @SerializedName("item_id7")       val itemId7: String? = null,
    @SerializedName("item_name7")     val itemName7: String? = null,
    @SerializedName("item_category7") val itemCategory7: String? = null,
    @SerializedName("price7")         val price7: String? = null,
    @SerializedName("quantity7")      val quantity7: String? = null,
    @SerializedName("imgsrc7")        val imgSrc7: String? = null,

    @SerializedName("item_id8")       val itemId8: String? = null,
    @SerializedName("item_name8")     val itemName8: String? = null,
    @SerializedName("item_category8") val itemCategory8: String? = null,
    @SerializedName("price8")         val price8: String? = null,
    @SerializedName("quantity8")      val quantity8: String? = null,
    @SerializedName("imgsrc8")        val imgSrc8: String? = null,

    @SerializedName("item_id9")       val itemId9: String? = null,
    @SerializedName("item_name9")     val itemName9: String? = null,
    @SerializedName("item_category9") val itemCategory9: String? = null,
    @SerializedName("price9")         val price9: String? = null,
    @SerializedName("quantity9")      val quantity9: String? = null,
    @SerializedName("imgsrc9")        val imgSrc9: String? = null,

    @SerializedName("item_id10")      val itemId10: String? = null,
    @SerializedName("item_name10")    val itemName10: String? = null,
    @SerializedName("item_category10")val itemCategory10: String? = null,
    @SerializedName("price10")        val price10: String? = null,
    @SerializedName("quantity10")     val quantity10: String? = null,
    @SerializedName("imgsrc10")       val imgSrc10: String? = null,
) {
    fun parsedItems(): List<CancelDetailItem> {
        val slots = listOf(
            listOf(itemId1, itemName1, itemCategory1, price1, quantity1, imgSrc1),
            listOf(itemId2, itemName2, itemCategory2, price2, quantity2, imgSrc2),
            listOf(itemId3, itemName3, itemCategory3, price3, quantity3, imgSrc3),
            listOf(itemId4, itemName4, itemCategory4, price4, quantity4, imgSrc4),
            listOf(itemId5, itemName5, itemCategory5, price5, quantity5, imgSrc5),
            listOf(itemId6, itemName6, itemCategory6, price6, quantity6, imgSrc6),
            listOf(itemId7, itemName7, itemCategory7, price7, quantity7, imgSrc7),
            listOf(itemId8, itemName8, itemCategory8, price8, quantity8, imgSrc8),
            listOf(itemId9, itemName9, itemCategory9, price9, quantity9, imgSrc9),
            listOf(itemId10, itemName10, itemCategory10, price10, quantity10, imgSrc10),
        )
        return slots
            .filter { !it[0].isNullOrBlank() }
            .map { s ->
                CancelDetailItem(
                    itemId       = s[0].orEmpty(),
                    itemName     = s[1].orEmpty(),
                    itemCategory = s[2].orEmpty(),
                    price        = s[3].orEmpty(),
                    quantity     = s[4].orEmpty(),
                    imgSrc       = s[5].orEmpty()
                )
            }
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
