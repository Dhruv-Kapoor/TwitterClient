package com.example.twitterclient.retrofit

import com.example.twitterclient.Credentials.CONSUMER_KEY
import com.example.twitterclient.Credentials.CONSUMER_SECRET
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TwitterClient {

    private  val interceptor = MyInterceptor()
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.twitter.com")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    val loginService = retrofit.create(LoginService::class.java)

    val apiService = retrofit.create(ApiService::class.java)

    fun setToken(key: String, secret: String){
        interceptor.setToken(key, secret)
    }

}

class MyInterceptor : Interceptor {

    private var TOKEN_KEY = ""
    private var TOKEN_SECRET = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val completeUrl = original.url().url()
        val url = completeUrl.protocol + "://" + completeUrl.host + completeUrl.path
        val params = HashMap<String,String>()

        if(completeUrl.query != null) {
            val queries = completeUrl.query.split("&", "=")
            for (i in queries.indices step 2) {
                params[queries[i]] = queries[i + 1]
            }
        }

        val header = TwitterOauthHeaderGenerator(CONSUMER_KEY, CONSUMER_SECRET, TOKEN_KEY,  TOKEN_SECRET)
            .generateHeader(original.method(),url, params)

        val req = original.newBuilder()
            .header("Authorization", header)
            .method(original.method(), original.body())
            .build()

        return chain.proceed(req)
    }

    fun setToken(key: String, secret: String){
        TOKEN_KEY = key
        TOKEN_SECRET = secret
    }

}