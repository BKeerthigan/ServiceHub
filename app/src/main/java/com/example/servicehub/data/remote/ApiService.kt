package com.example.servicehub.remote


import com.example.servicehub.data.model.AccountListResponse
import com.example.servicehub.data.model.OrderDetailsResponse
import com.example.servicehub.data.model.PlaceOrderResponse
import com.example.servicehub.data.model.ProceedToBuyResponse
import com.example.servicehub.data.model.QrCodeResponse
import com.example.servicehub.data.model.AdListResponse
import com.example.servicehub.data.model.PolicyResponse
import com.example.servicehub.data.model.SchemeListResponse
import com.example.servicehub.data.model.TargetSchemeResponse
import com.example.servicehub.data.model.AddExpiryReturnResponse
import com.example.servicehub.data.model.EditAddressResponse
import com.example.servicehub.data.model.EditNameResponse
import com.example.servicehub.data.model.YourDetailsResponse
import com.example.servicehub.data.model.ExpiryListResponse
import com.example.servicehub.data.model.ExpiryReturnedResponse
import com.example.servicehub.data.model.CancelDetailResponse
import com.example.servicehub.data.model.CreateReturnResponse
import com.example.servicehub.data.model.ReturnListResponse
import com.example.servicehub.data.model.ReturnedOrdersResponse
import com.example.servicehub.data.model.CancelReorderResponse
import com.example.servicehub.data.model.CancelledListResponse
import com.example.servicehub.data.model.DeliveryListResponse
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
        @Field("pincode") pincode: String,
        @Field("latitude_number") latitude: String,
        @Field("longitude_number") longitude: String,
        @Field("sales_lead") salesLead: Int,
        @Field("sales_person") salesPerson: Int
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

    @GET("mapi/sapiaccountlist.php")
    suspend fun getAccountList(
        @Query("company_id") companyId: String
    ): AccountListResponse

    @GET("mapi/sapideliverylist.php")
    suspend fun getDeliveryList(
        @Query("company_id") companyId: String
    ): DeliveryListResponse

    @GET("mapi/sapireturnlist.php")
    suspend fun getReturnList(
        @Query("company_id") companyId: String
    ): ReturnListResponse

    @GET("mapi/sapireturnedorders.php")
    suspend fun getReturnedOrders(
        @Query("company_id") companyId: String
    ): ReturnedOrdersResponse

    @FormUrlEncoded
    @POST("mapi/sapicreatereturn.php")
    suspend fun createReturn(
        @Field("sales_order_id") salesOrderId: String,
        @Field("company_id") companyId: String,
        @Field("item_id") itemId: String,
        @Field("reason") reason: String,
        @Field("quantity") quantity: Int
    ): CreateReturnResponse

    @GET("mapi/sapicancellist.php")
    suspend fun getCancelledList(
        @Query("company_id") companyId: String
    ): CancelledListResponse

    @GET("mapi/sapicanceldetails.php")
    suspend fun getCancelDetails(
        @Query("sales_order_id") salesOrderId: String
    ): CancelDetailResponse

    @GET("mapi/sapideliverydetails.php")
    suspend fun getDeliveryDetails(
        @Query("sales_order_id") salesOrderId: String
    ): CancelDetailResponse

    @FormUrlEncoded
    @POST("mapi/sapicancelreorder.php")
    suspend fun cancelOrReorder(
        @Field("sales_order_id") salesOrderId: String,
        @Field("reason") reason: String
    ): CancelReorderResponse

    @GET("mapi/sapiyourdetails.php")
    suspend fun getYourDetails(
        @Query("company_id") companyId: String
    ): YourDetailsResponse

    @FormUrlEncoded
    @POST("mapi/sapieditname.php")
    suspend fun editName(
        @Field("company_id") companyId: String,
        @Field("contact_name") contactName: String
    ): EditNameResponse

    @FormUrlEncoded
    @POST("mapi/sapieditaddress.php")
    suspend fun editAddress(
        @Field("company_id") companyId: String,
        @Field("address") address: String,
        @Field("landmark") landmark: String,
        @Field("city") city: String,
        @Field("pincode") pincode: String,
        @Field("latitude_number") latitude: String,
        @Field("longitude_number") longitude: String
    ): EditAddressResponse

    @GET("mapi/sapiexpirylist.php")
    suspend fun getExpiryList(
        @Query("company_id") companyId: String
    ): ExpiryListResponse

    @GET("mapi/sapiexpiryreturned.php")
    suspend fun getExpiryReturned(
        @Query("company_id") companyId: String
    ): ExpiryReturnedResponse

    @FormUrlEncoded
    @POST("mapi/sapiaddexpiryitem.php")
    suspend fun addExpiryReturn(
        @Field("sales_order_id") salesOrderId: String,
        @Field("company_id") companyId: String,
        @Field("item_id") itemId: String,
        @Field("mfg_date") mfgDate: String,
        @Field("exp_date") expDate: String,
        @Field("quantity") quantity: Int
    ): AddExpiryReturnResponse

    @GET("mapi/sapipolicyinfo.php")
    suspend fun getPolicyInfo(): PolicyResponse

    @GET("mapi/sapitargetscheme.php")
    suspend fun getTargetSchemeInfo(): TargetSchemeResponse

    @GET("mapi/sapiactivescheme.php")
    suspend fun getActiveSchemes(
        @Query("company_id") companyId: String
    ): SchemeListResponse

    @GET("mapi/sapipastscheme.php")
    suspend fun getPastSchemes(
        @Query("company_id") companyId: String
    ): SchemeListResponse

    @FormUrlEncoded
    @POST("mapi/sapicartdelete.php")
    suspend fun removeFromCart(
        @Field("company_id") companyId: String,
        @Field("item_id") itemId: String,
        @Field("quantity") quantity: Int,
        @Field("price") price: String
    ): CartResponse

    @GET("mapi/sapiproceedtobuy.php")
    suspend fun getProceedToBuyInfo(
        @Query("mobile_app") mobileApp: String
    ): ProceedToBuyResponse

    @FormUrlEncoded
    @POST("mapi/sapicodorder.php")
    suspend fun placeCodOrder(
        @Field("company_id") companyId: String,
        @Field("latitude_number") lat: String,
        @Field("longitude_number") lng: String
    ): PlaceOrderResponse

    @GET("mapi/sapiorderdetails.php")
    suspend fun getOrderDetails(
        @Query("sales_order_id") salesOrderId: String
    ): OrderDetailsResponse

    @GET("mapi/sapiqrcodedetails.php")
    suspend fun getQrCode(): QrCodeResponse

    @FormUrlEncoded
    @POST("mapi/sapiqrorder.php")
    suspend fun placeQrOrder(
        @Field("company_id")        companyId: String,
        @Field("latitude_number")   lat: String,
        @Field("longitude_number")  lng: String,
        @Field("trans_ref")         transRef: String,
        @Field("trans_date")        transDate: String
    ): PlaceOrderResponse
}
