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

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("status")
	val status: String?=null


)
