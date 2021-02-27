package com.example.twitterclient.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.twitterclient.dataClasses.User
import com.example.twitterclient.dataClasses.UsersResponse
import com.example.twitterclient.fragments.HomeFragmentViewModel.TweetRange
import com.example.twitterclient.retrofit.TwitterClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowersActivityViewModel : ViewModel() {

    private val tweetRange = MutableLiveData<TweetRange>()
    private var cursor: Long = -1
    private val list = MutableLiveData(ArrayList<User>()).also {
        loadFollowers()
    }
    val logOut = MutableLiveData(false)

    fun getFollowers(): LiveData<ArrayList<User>> = list
    fun getTweetRange() = tweetRange as LiveData<TweetRange>
    fun getLogOutStatus() = logOut as LiveData<Boolean>

    private fun loadFollowers() {
        TwitterClient.apiService.getFollowers(cursor).enqueue(object : Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                if (response.code().div(100) == 4) {
                    logOut.postValue(true)
                }
                val newList = response.body()?.users
                if (response.isSuccessful && newList != null) {
                    val temp = list.value
                    val range = TweetRange(temp?.size!!, newList.size)
                    temp.addAll(newList)
                    list.postValue(temp)
                    tweetRange.postValue(range)
                    cursor = response.body()!!.next_cursor!!
                }
            }

            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
            }

        })
    }

    fun updateFollowStatus(id: Long, followed: Boolean) {
        if (followed) {
            TwitterClient.apiService.follow(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }

            })
        } else {
            TwitterClient.apiService.unFollow(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }

            })
        }
    }
}