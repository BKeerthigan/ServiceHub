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

// Fixed: was "pid", API actually returns "piid"
data class ProductItem(
    @SerializedName("piid") val piid: String? = null,
    @SerializedName("typeid") val typeId: String? = null,
    @SerializedName("productid") val productId: String? = null,
    @SerializedName("listid") val listId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("nutrition") val nutrition: String? = null,
    @SerializedName("imgsrc") val imgsrc: String? = null
)

// Products under a sub-category — sapiitemlistbyid.php?piid=X
data class ItemDetailResponse(
    @SerializedName("data") val data: List<ItemDetail> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class ItemDetail(
    @SerializedName("item_id") val itemId: String? = null,
    @SerializedName("piid") val piid: String? = null,
    @SerializedName("item_name") val itemName: String? = null,
    @SerializedName("stock_id") val stockId: String? = null,
    @SerializedName("stock") val stock: String? = null,
    @SerializedName("item_code") val itemCode: String? = null,
    @SerializedName("item_category") val itemCategory: String? = null,
    @SerializedName("item_price") val itemPrice: String? = null,
    @SerializedName("profile_id") val profileId: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("imgsrc") val imgsrc: String? = null,
    @SerializedName("del_info") val delInfo: String? = null,
    @SerializedName("descripition") val description: String? = null,
    @SerializedName("nutrition") val nutrition: String? = null
)

// Weight filter chips — sapicategorylistbyid.php?piid=X
data class CategoryListResponse(
    @SerializedName("data") val data: List<CategoryItem> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class CategoryItem(
    @SerializedName("item_category") val itemCategory: String? = null
)

data class CartResponse(
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

// Cart details screen — sapicartdetails.php?company_id=X
data class CartDetailsResponse(
    @SerializedName("data") val data: List<CartDetails> = emptyList(),
    @SerializedName("success") val success: Int = 0,
    @SerializedName("message") val message: String? = null
)

data class CartItemApi(
    @SerializedName("item_id")       val itemId: String? = null,
    @SerializedName("item_name")     val itemName: String? = null,
    @SerializedName("item_category") val itemCategory: String? = null,
    @SerializedName("price")         val price: String? = null,
    @SerializedName("quantity")      val quantity: String? = null,
    @SerializedName("imgsrc")        val imgsrc: String? = null,
)

data class CartDetails(
    @SerializedName("company_address") val companyAddress: String? = null,
    @SerializedName("del_info")        val delInfo: String? = null,
    @SerializedName("shipping")        val shipping: String? = null,
    @SerializedName("shipping_charge") val shippingCharge: String? = null,
    @SerializedName("cancellation")    val cancellation: String? = null,
    @SerializedName("returns")         val returns: String? = null,
    @SerializedName("read_policy")     val readPolicy: String? = null,
    @SerializedName("policy_src")      val policySrc: String? = null,
    @SerializedName("item")            val items: List<CartItemApi> = emptyList(),
) {
    fun parsedItems(): List<CartItemFlat> = items.mapNotNull { api ->
        val name = api.itemName ?: return@mapNotNull null
        CartItemFlat(
            itemId   = api.itemId.orEmpty(),
            name     = name,
            category = api.itemCategory.orEmpty(),
            price    = api.price.orEmpty(),
            quantity = api.quantity.orEmpty(),
            imgsrc   = api.imgsrc.orEmpty()
        )
    }
}

data class CartItemFlat(
    val itemId: String,
    val name: String,
    val category: String,
    val price: String,
    val quantity: String,
    val imgsrc: String
)
