package com.example.servicehub.remote


import com.example.servicehub.data.model.AdListResponse
import com.example.servicehub.data.model.CartDetailsResponse
import com.example.servicehub.data.model.CartResponse
import com.example.servicehub.data.model.CategoryListResponse
import com.example.servicehub.data.model.ItemDetailResponse
import com.example.servicehub.data.model.LoginResponse
import com.example.servicehub.data.model.ProductDetailsResponse
import com.example.servicehub.data.model.ProductItemResponse
import com.example.servicehub.data.model.ProductListResponse
import com.example.servicehub.data.model.RegisterResponse
import com.example.servicehub.data.model.TypeListResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("mapi/sapicheckflag.php")
    suspend fun checkLoginFlag(
        @Query("mobile_app") mobile: String
    ): Response<LoginResponse>


    @FormUrlEncoded
    @POST("mapi/sapiregdetails.php")
    suspend fun registerDetails(
        @Field("mobile_app") mobile: String,
        @Field("contact_name") contactName: String,
        @Field("company_name") companyName: String,
        @Field("address") address: String,
        @Field("landmark") landmark: String,
        @Field("city") city: String,
        @Field("pincode") pincode: String
    ): Response<RegisterResponse>


    @GET("mapi/sapitypelist.php")
    suspend fun getTypeList(): Response<TypeListResponse>

    @GET("mapi/sapiadlist.php")
    suspend fun getAdList(): Response<AdListResponse>

    @GET("mapi/sapiproductdetails.php")
    suspend fun getProductDetails(): ProductDetailsResponse

    @GET("mapi/sapiproductlist.php")
    suspend fun getProductList(): ProductListResponse

    @GET("mapi/sapiproductitemlistbyid.php")
    suspend fun getProductItems(
        @Query("listid") listId: String
    ): ProductItemResponse

    @GET("mapi/sapiitemlistbyid.php")
    suspend fun getItemList(
        @Query("piid") piid: String
    ): ItemDetailResponse

    @GET("mapi/sapicategorylistbyid.php")
    suspend fun getCategoryList(
        @Query("piid") piid: String
    ): CategoryListResponse

    @GET("mapi/sapiitembyid.php")
    suspend fun getItemById(
        @Query("item_id") itemId: String
    ): ItemDetailResponse

    @GET("mapi/sapicartdetails.php")
    suspend fun getCartDetails(
        @Query("company_id") companyId: String
    ): CartDetailsResponse

    @FormUrlEncoded
    @POST("mapi/sapicartadd.php")
    suspend fun addToCart(
        @Field("company_id") companyId: String,
        @Field("item_id") itemId: String,
        @Field("quantity") quantity: Int,
        @Field("price") price: String
    ): CartResponse

    @FormUrlEncoded
    @POST("mapi/sapicartdelete.php")
    suspend fun removeFromCart(
        @Field("company_id") companyId: String,
        @Field("item_id") itemId: String,
        @Field("quantity") quantity: Int,
        @Field("price") price: String
    ): CartResponse
}
