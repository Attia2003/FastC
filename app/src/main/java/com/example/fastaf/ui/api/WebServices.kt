package com.example.fastaf.ui.api

import com.example.fastaf.ui.home.searchable.ResponseSearchRec
import com.example.fastaf.ui.home.searchable.ResponseSearchRecItem

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface WebServices {

      @GET("drugs")
     suspend  fun getDrugs(@Query("page") page: Int,@Query("size") size: Int, @Query("form") form: String?)
     : Response<MutableList<ResponseSearchRecItem>>

      @PATCH("drugs/status")
      suspend fun getUpdateStatus(@Query("id") id: Int,@Query("status") status: String):Response<Unit>

}