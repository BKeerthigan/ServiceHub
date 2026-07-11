package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class ProceedToBuyResponse(
    @SerializedName("data")    val data: List<ProceedToBuyData>?,
    @SerializedName("success") val success: Int?,
    @SerializedName("message") val message: String?
)

data class ProceedToBuyData(
    @SerializedName("check_flag")   val checkFlag: String?,
    @SerializedName("amount")       val amount: String?,       // COD amount
    @SerializedName("cod")          val cod: String?,
    @SerializedName("cod_notes")    val codNotes: String?,
    @SerializedName("qrcode")       val qrcode: String?,
    @SerializedName("qrcode_notes") val qrcodeNotes: String?,
    @SerializedName("qr_amount")    val qrAmount: Double?      // QR discounted amount (API returns number)
)

data class QrCodeResponse(
    @SerializedName("data")    val data: List<QrCodeData>?,
    @SerializedName("success") val success: Int?,
    @SerializedName("message") val message: String?
)

data class QrCodeData(
    @SerializedName("address")    val address: String?,
    @SerializedName("qrcode_img") val qrcodeImg: String?
)

data class PlaceOrderResponse(
    @SerializedName("success")            val success: Int?,
    @SerializedName("order_id")           val orderId: Int?,
    @SerializedName("sales_order_number") val salesOrderNumber: String?,
    @SerializedName("delivery_date")      val deliveryDate: String?,
    @SerializedName("message")            val message: String?
)
