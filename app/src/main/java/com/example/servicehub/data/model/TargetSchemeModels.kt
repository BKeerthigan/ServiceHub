package com.example.servicehub.data.model

data class TargetSchemeInfo(
    val imgsrc: String? = null,
    val active: String? = null,
    val past: String? = null,
    val content: String? = null
)

data class TargetSchemeResponse(
    val data: List<TargetSchemeInfo>? = null,
    val success: Int? = null,
    val message: String? = null
)

// Active / Past scheme item — fields ready for when backend deploys
data class SchemeItem(
    val scheme_id: String? = null,
    val scheme_name: String? = null,
    val target_amount: String? = null,
    val achieved_amount: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val reward: String? = null,
    val status: String? = null,
    val imgsrc: String? = null
)

data class SchemeListResponse(
    val data: List<SchemeItem>? = null,
    val success: Int? = null,
    val message: String? = null
)
