package com.example.fastaf.ui.api

import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem
import com.example.fastaf.ui.input.DrugCreationRequest
import com.example.fastaf.ui.login.UserInfoResponse
import okhttp3.MultipartBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface WebServices {

     @GET("drugs/not-imaged")
     suspend  fun getDrugs(@Query("page") page: String,
                           @Query("size") size: Int,
                           @Query("name") name:String="",
                           @Query("username")username:String?
      )
     : Response<MutableList<ResponseSearchRecItem>>

      @PATCH("drugs/status")
      suspend fun getUpdateStatus(@Query("id") id: Int,@Query("status") status: String):Response<Unit>

      @Multipart
      @POST("images/upload")
      suspend fun uploadImage(@Query("username") username: String,@Query("drugID") id: Int, @Part file: MultipartBody.Part):Response<Unit>

      @GET("users/info")
      suspend fun getUserInfo(@Query("username") username: String):Response<UserInfoResponse>

      @POST("users/create")
      suspend fun createUser(@Body request: UserInfoResponse):Response<Unit>

      @POST("drugs")
      suspend fun createDRUG(  @Body request: DrugCreationRequest):Response<Unit>



}
