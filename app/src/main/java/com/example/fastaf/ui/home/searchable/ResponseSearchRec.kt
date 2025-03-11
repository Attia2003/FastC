package com.example.fastaf.ui.home.searchable

import com.google.gson.annotations.SerializedName

data class ResponseSearchRec(

	@field:SerializedName("results")
	val results: List<ResponseSearchRecItem>
)

data class ResponseSearchRecItem(

    @field:SerializedName("createdAt")
	val createdAt: String? = null,

    @field:SerializedName("name")
	val name: String? = null,

    @field:SerializedName("form")
	val form: String? = null,

    @field:SerializedName("id")
	val id: Int,

    @field:SerializedName("updatedAt")
	val updatedAt: String? = null,

    @field:SerializedName("status")
	var status: String?=null


)
