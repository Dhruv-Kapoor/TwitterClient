package com.example.twitterclient.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.twitterclient.dataClasses.Trend
import com.example.twitterclient.dataClasses.TrendsResponse
import com.example.twitterclient.retrofit.TwitterClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrendsViewModel : ViewModel() {
    private val DELHI_WOEID = 20070458

    private val trends = ArrayList<Trend>()
    private val trendsLiveData = MutableLiveData(trends)
    private val isRefreshing = MutableLiveData(false)
    private val logOut = MutableLiveData(false)
    private var retryCount = 0

    fun getTrendsLiveData() = trendsLiveData as LiveData<ArrayList<Trend>>
    fun getRefreshStatus() = isRefreshing as LiveData<Boolean>
    fun getLogOutStatus() = logOut as LiveData<Boolean>

    fun getTrends(){
        if(trends.isNotEmpty()){
           return
        }
        isRefreshing.postValue(true)
        TwitterClient.apiService.getTrends(DELHI_WOEID).enqueue(object: Callback<TrendsResponse> {
            override fun onResponse(
                call: Call<TrendsResponse>,
                response: Response<TrendsResponse>
            ) {
                if(response.code().div(100) == 4){
                    logOut.postValue(true)
                }
                trendsLiveData.postValue(response.body()?.get(0)?.trends!!)
                isRefreshing.postValue(false)
            }

            override fun onFailure(call: Call<TrendsResponse>, t: Throwable) {
                retryCount++
                if(retryCount>3){
                    isRefreshing.postValue(false)
                    return
                }
                getTrends()
            }

        })
    }

}