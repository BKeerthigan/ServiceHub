package com.example.servicehub.remote


import com.example.servicehub.data.model.AdListResponse
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
}
