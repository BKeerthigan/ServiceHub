package com.example.servicehub.data.model

import com.google.gson.annotations.SerializedName

data class PolicyItem(
    val tandc: String? = null,
    val privacy: String? = null,
    @SerializedName("return")  val returnPolicy: String? = null,
    val delivery: String? = null
)

data class PolicySourceItem(
    @SerializedName("tandc_src")    val tandcSrc: String? = null,
    @SerializedName("privacy_src")  val privacySrc: String? = null,
    @SerializedName("return_src")   val returnSrc: String? = null,
    @SerializedName("delivery_src") val deliverySrc: String? = null
)

data class PolicyData(
    @SerializedName("Policies_image")  val policiesImage: String? = null,
    @SerializedName("Policies")        val policies: List<PolicyItem>? = null,
    @SerializedName("Policies_Source") val policiesSrc: List<PolicySourceItem>? = null
)

data class PolicyResponse(
    val data: List<PolicyData>? = null,
    val success: Int? = null,
    val message: String? = null
)
