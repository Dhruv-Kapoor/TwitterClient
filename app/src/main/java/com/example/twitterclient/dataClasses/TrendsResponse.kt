package com.example.twitterclient.dataClasses

class TrendsResponse : ArrayList<Trends>()

data class Trends(
    val as_of: String?,
    val created_at: String?,
    val locations: List<Location>?,
    val trends: ArrayList<Trend>?
)

data class Trend(
    val name: String?,
    val promoted_content: String?,
    val query: String?,
    val tweet_volume: Int?,
    val url: String?
)

data class Location(
    val name: String?,
    val woeid: Int?
)