package com.example.twitterclient.retrofit

import com.example.twitterclient.dataClasses.*
import com.example.twitterclient.fragments.DEFAULT_TWEET_COUNT
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("2/tweets/search/recent")
    fun searchTweets(@Query("query") query: String): Call<SearchResponse>

    @GET("1.1/statuses/home_timeline.json")
    fun getHomeTimelineLatest(
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("count") count: Int = DEFAULT_TWEET_COUNT
    ): Call<TweetTimeLine>

    @GET("1.1/statuses/home_timeline.json")
    fun getHomeTimelineSince(
        @Query("since_id") since_id: Long,
        @Query("tweet_mode") tweetMode: String = "extended"
    ): Call<TweetTimeLine>

    @GET("1.1/statuses/home_timeline.json")
    fun getHomeTimelineBefore(
        @Query("max_id") max_id: Long,
        @Query("count") count: Int = DEFAULT_TWEET_COUNT,
        @Query("tweet_mode") tweetMode: String = "extended"
    ): Call<TweetTimeLine>

    @POST("1.1/favorites/create.json")
    fun createLike(
        @Query("id") id: Long
    ): Call<ResponseBody>

    @POST("1.1/favorites/destroy.json")
    fun destroyLike(
        @Query("id") id: Long
    ): Call<ResponseBody>

    @POST("1.1/statuses/retweet/{id}.json")
    fun createRetweet(
        @Path("id") id: Long
    ): Call<ResponseBody>

    @POST("1.1/statuses/unretweet/{id}.json")
    fun destroyRetweet(
        @Path("id") id: Long
    ): Call<ResponseBody>

    @GET("1.1/trends/place.json")
    fun getTrends(
        @Query("id") id:Int
    ): Call<TrendsResponse>

    @POST("1.1/friendships/create.json")
    fun follow(
        @Query("user_id") id:Long
    ): Call<ResponseBody>

    @POST("1.1/friendships/destroy.json")
    fun unFollow(
        @Query("user_id") id:Long
    ): Call<ResponseBody>

    @GET("1.1/users/show.json")
    fun getUser(
        @Query("user_id") id: Long
    ): Call<User>

    @GET("1.1/users/show.json")
    fun getUserByName(
        @Query("screen_name") screen_name: String
    ): Call<User>

    @GET("1.1/account/verify_credentials.json")
    fun verifyCredentials(): Call<User>

    @GET("1.1/followers/list.json")
    fun getFollowers(
        @Query("cursor") cursor: Long = -1L,
        @Query("count") count: Int = 100
    ): Call<UsersResponse>

    @GET("1.1/friends/list.json")
    fun getFollowing(
        @Query("cursor") cursor: Long = -1L,
        @Query("count") count: Int = 100
    ): Call<UsersResponse>
}