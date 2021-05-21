package com.android.farmdoctor.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class DataItem(

	@field:SerializedName("family_common_name")
	val familyCommonName: String,

	@field:SerializedName("year")
	val year: Int,

	@field:SerializedName("genus_id")
	val genusId: Int,

	@field:SerializedName("author")
	val author: String,

	@field:SerializedName("image_url")
	val imageUrl: String,

	@field:SerializedName("synonyms")
	val synonyms: List<String>,

	@field:SerializedName("scientific_name")
	val scientificName: String,

	@field:SerializedName("bibliography")
	val bibliography: String,

	@field:SerializedName("genus")
	val genus: String,

	@field:SerializedName("rank")
	val rank: String,

	@field:SerializedName("links")
	val links: Links,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("common_name")
	val commonName: String,

	@field:SerializedName("family")
	val family: String,

	@field:SerializedName("slug")
	val slug: String,

	@field:SerializedName("status")
	val status: String
)