package com.android.farmdoctor.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class Links(

	@field:SerializedName("next")
	val next: String,

	@field:SerializedName("last")
	val last: String,

	@field:SerializedName("prev")
	val prev: String,

	@field:SerializedName("self")
	val self: String,

	@field:SerializedName("first")
	val first: String,

	@field:SerializedName("genus")
	val genus: String,

	@field:SerializedName("plant")
	val plant: String
)