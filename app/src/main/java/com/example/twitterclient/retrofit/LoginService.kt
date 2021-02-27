package com.example.twitterclient.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginService {
    @POST("/oauth/request_token")
    fun requestToken(): Call<ResponseBody>

    @POST("oauth/access_token")
    fun accessToken(
        @Query("oauth_token") token: String,
        @Query("oauth_verifier") verifier: String
    ): Call<ResponseBody>

    @GET("1.1/account/verify_credentials.json")
    fun verifyLogin(): Call<ResponseBody>
}

