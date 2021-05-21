package com.android.farmdoctor.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class Meta(

	@field:SerializedName("total")
	val total: Int
)