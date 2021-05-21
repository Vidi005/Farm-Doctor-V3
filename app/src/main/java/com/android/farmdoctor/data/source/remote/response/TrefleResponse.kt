package com.android.farmdoctor.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class TrefleResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("meta")
	val meta: Meta,

	@field:SerializedName("links")
	val links: Links
)