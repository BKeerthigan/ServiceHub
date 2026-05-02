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

data class CartDetails(
    @SerializedName("company_address") val companyAddress: String? = null,
    @SerializedName("del_info")        val delInfo: String? = null,
    @SerializedName("shipping")        val shipping: String? = null,
    @SerializedName("shipping_charge") val shippingCharge: String? = null,
    @SerializedName("cancellation")    val cancellation: String? = null,
    @SerializedName("returns")         val returns: String? = null,
    @SerializedName("read_policy")     val readPolicy: String? = null,
    // Flat item fields — API returns item_id1, item_name1, item_name2 … up to 10
    @SerializedName("item_id1")       val itemId1: String? = null,
    @SerializedName("item_name1")     val itemName1: String? = null,
    @SerializedName("item_category1") val itemCategory1: String? = null,
    @SerializedName("price1")         val price1: String? = null,
    @SerializedName("quantity1")      val quantity1: String? = null,
    @SerializedName("imgsrc1")        val imgsrc1: String? = null,
    @SerializedName("item_id2")       val itemId2: String? = null,
    @SerializedName("item_name2")     val itemName2: String? = null,
    @SerializedName("item_category2") val itemCategory2: String? = null,
    @SerializedName("price2")         val price2: String? = null,
    @SerializedName("quantity2")      val quantity2: String? = null,
    @SerializedName("imgsrc2")        val imgsrc2: String? = null,
    @SerializedName("item_id3")       val itemId3: String? = null,
    @SerializedName("item_name3")     val itemName3: String? = null,
    @SerializedName("item_category3") val itemCategory3: String? = null,
    @SerializedName("price3")         val price3: String? = null,
    @SerializedName("quantity3")      val quantity3: String? = null,
    @SerializedName("imgsrc3")        val imgsrc3: String? = null,
    @SerializedName("item_id4")       val itemId4: String? = null,
    @SerializedName("item_name4")     val itemName4: String? = null,
    @SerializedName("item_category4") val itemCategory4: String? = null,
    @SerializedName("price4")         val price4: String? = null,
    @SerializedName("quantity4")      val quantity4: String? = null,
    @SerializedName("imgsrc4")        val imgsrc4: String? = null,
    @SerializedName("item_id5")       val itemId5: String? = null,
    @SerializedName("item_name5")     val itemName5: String? = null,
    @SerializedName("item_category5") val itemCategory5: String? = null,
    @SerializedName("price5")         val price5: String? = null,
    @SerializedName("quantity5")      val quantity5: String? = null,
    @SerializedName("imgsrc5")        val imgsrc5: String? = null,
    @SerializedName("item_id6")       val itemId6: String? = null,
    @SerializedName("item_name6")     val itemName6: String? = null,
    @SerializedName("item_category6") val itemCategory6: String? = null,
    @SerializedName("price6")         val price6: String? = null,
    @SerializedName("quantity6")      val quantity6: String? = null,
    @SerializedName("imgsrc6")        val imgsrc6: String? = null,
    @SerializedName("item_id7")       val itemId7: String? = null,
    @SerializedName("item_name7")     val itemName7: String? = null,
    @SerializedName("item_category7") val itemCategory7: String? = null,
    @SerializedName("price7")         val price7: String? = null,
    @SerializedName("quantity7")      val quantity7: String? = null,
    @SerializedName("imgsrc7")        val imgsrc7: String? = null,
    @SerializedName("item_id8")       val itemId8: String? = null,
    @SerializedName("item_name8")     val itemName8: String? = null,
    @SerializedName("item_category8") val itemCategory8: String? = null,
    @SerializedName("price8")         val price8: String? = null,
    @SerializedName("quantity8")      val quantity8: String? = null,
    @SerializedName("imgsrc8")        val imgsrc8: String? = null,
    @SerializedName("item_id9")       val itemId9: String? = null,
    @SerializedName("item_name9")     val itemName9: String? = null,
    @SerializedName("item_category9") val itemCategory9: String? = null,
    @SerializedName("price9")         val price9: String? = null,
    @SerializedName("quantity9")      val quantity9: String? = null,
    @SerializedName("imgsrc9")        val imgsrc9: String? = null,
    @SerializedName("item_id10")      val itemId10: String? = null,
    @SerializedName("item_name10")     val itemName10: String? = null,
    @SerializedName("item_category10") val itemCategory10: String? = null,
    @SerializedName("price10")         val price10: String? = null,
    @SerializedName("quantity10")      val quantity10: String? = null,
    @SerializedName("imgsrc10")        val imgsrc10: String? = null,
) {
    fun parsedItems(): List<CartItemFlat> {
        val raw = listOf(
            listOf(itemId1, itemName1, itemCategory1, price1, quantity1, imgsrc1),
            listOf(itemId2, itemName2, itemCategory2, price2, quantity2, imgsrc2),
            listOf(itemId3, itemName3, itemCategory3, price3, quantity3, imgsrc3),
            listOf(itemId4, itemName4, itemCategory4, price4, quantity4, imgsrc4),
            listOf(itemId5, itemName5, itemCategory5, price5, quantity5, imgsrc5),
            listOf(itemId6, itemName6, itemCategory6, price6, quantity6, imgsrc6),
            listOf(itemId7, itemName7, itemCategory7, price7, quantity7, imgsrc7),
            listOf(itemId8, itemName8, itemCategory8, price8, quantity8, imgsrc8),
            listOf(itemId9, itemName9, itemCategory9, price9, quantity9, imgsrc9),
            listOf(itemId10, itemName10, itemCategory10, price10, quantity10, imgsrc10),
        )
        return raw.mapNotNull { fields ->
            val name = fields[1] ?: return@mapNotNull null
            CartItemFlat(
                itemId   = fields[0].orEmpty(),
                name     = name,
                category = fields[2].orEmpty(),
                price    = fields[3].orEmpty(),
                quantity = fields[4].orEmpty(),
                imgsrc   = fields[5].orEmpty()
            )
        }
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
