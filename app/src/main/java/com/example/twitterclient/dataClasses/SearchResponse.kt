package com.example.twitterclient.dataClasses

data class SearchResponse(
	val data: List<DataItem?>? = null,
	val meta: Meta? = null
)

data class DataItem(
	val id: String? = null,
	val text: String? = null
)

data class Meta(
	val oldestId: String? = null,
	val newestId: String? = null,
	val nextToken: String? = null,
	val resultCount: Int? = null
)

