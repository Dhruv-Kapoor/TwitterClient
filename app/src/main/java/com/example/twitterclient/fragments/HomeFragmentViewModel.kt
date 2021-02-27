package com.example.twitterclient.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.twitterclient.dataClasses.Tweet
import com.example.twitterclient.dataClasses.TweetTimeLine
import com.example.twitterclient.retrofit.TwitterClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeFragmentViewModel"
const val DEFAULT_TWEET_COUNT = 50

class HomeFragmentViewModel : ViewModel() {

    var firstTweetId: Long? = null

    private val timeline = MutableLiveData(TweetTimeLine())
    private val isRefreshing = MutableLiveData(false)
    private val failureCount = MutableLiveData(0)
    private val tweetRange = MutableLiveData<TweetRange>()
    private val updateScrollPos = MutableLiveData(false)
    var scrollPosition = 0
    private val logOut = MutableLiveData(false)
    private val tooManyRequests = MutableLiveData(false)

    fun getTimeLine() = timeline as LiveData<TweetTimeLine>
    fun refreshStatus() = isRefreshing as LiveData<Boolean>
    fun getFailureCount() = failureCount as LiveData<Int>
    fun getTweetRange() = tweetRange as LiveData<TweetRange>
    fun getScrollPosUpdate() = updateScrollPos as LiveData<Boolean>
    fun getLogOutStatus() = logOut as LiveData<Boolean>
    fun getTooManyRequestsStatus() = tooManyRequests as LiveData<Boolean>

    fun init(count: Int) {
        if (firstTweetId == null || firstTweetId == -1L) {
            loadTop50Tweets()
            return
        }
        TwitterClient.apiService.getHomeTimelineBefore(firstTweetId!!, count)
            .enqueue(object : Callback<TweetTimeLine> {
                override fun onResponse(
                    call: Call<TweetTimeLine>,
                    response: Response<TweetTimeLine>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                    if (!response.isSuccessful || response.body() == null) {
                        failureCount.postValue(failureCount.value?.plus(1))
                        return
                    }
                    if(response.body()!!.isEmpty()){
                        return
                    }
                    val list = timeline.value
                    val range = TweetRange(list!!.size, response.body()!!.size)
                    list.addAll(response.body() as List<Tweet>)
                    timeline.postValue(list)
                    tweetRange.postValue(range)
                    updateScrollPos.postValue(true)
                }

                override fun onFailure(call: Call<TweetTimeLine>, t: Throwable) {
                    failureCount.postValue(failureCount.value?.plus(1))
                }

            })
    }

    fun refresh() {
        isRefreshing.postValue(true)
        loadLatestTweets()
    }

    fun loadLatestTweets() {
        if (firstTweetId == null || firstTweetId == -1L) {
            loadTop50Tweets()
            return
        }
        TwitterClient.apiService.getHomeTimelineSince(firstTweetId!!)
            .enqueue(object : Callback<TweetTimeLine> {
                override fun onResponse(
                    call: Call<TweetTimeLine>,
                    response: Response<TweetTimeLine>
                ) {
                    isRefreshing.postValue(false)
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                    if (!response.isSuccessful || response.body() == null) {
                        failureCount.postValue(failureCount.value?.plus(1))
                        return
                    }
                    if(response.body()!!.isEmpty()){
                        return
                    }
                    val range = TweetRange(0, response.body()!!.size)
                    val list = timeline.value
                    response.body()?.addAll(list as List<Tweet>)
                    timeline.postValue(response.body())
                    tweetRange.postValue(range)
                    firstTweetId = response.body()?.get(0)?.id
                }

                override fun onFailure(call: Call<TweetTimeLine>, t: Throwable) {
                    failureCount.postValue(failureCount.value?.plus(1))
                    isRefreshing.postValue(false)
                }

            })
    }

    fun loadTop50Tweets() {
        TwitterClient.apiService.getHomeTimelineLatest().enqueue(object : Callback<TweetTimeLine> {
            override fun onResponse(call: Call<TweetTimeLine>, response: Response<TweetTimeLine>) {
                isRefreshing.postValue(false)
                if (response.code() == 429) {
                    tooManyRequests.postValue(true)
                    return
                }
                if (response.code().div(100) == 4) {
                    logOut.postValue(true)
                    return
                }
                if (!response.isSuccessful || response.body() == null) {
                    failureCount.postValue(failureCount.value?.plus(1) ?: 1)
                    return
                }
                if(response.body()!!.isEmpty()){
                    return
                }
                timeline.postValue(response.body())
                tweetRange.postValue(TweetRange(0, 50))
                firstTweetId = response.body()?.get(0)?.id
            }

            override fun onFailure(call: Call<TweetTimeLine>, t: Throwable) {
                failureCount.postValue(failureCount.value?.plus(1))
                isRefreshing.postValue(false)
            }

        })
    }

    fun loadOldTweets(lastTweetId: Long, count: Int = DEFAULT_TWEET_COUNT) {
        TwitterClient.apiService.getHomeTimelineBefore(lastTweetId, count)
            .enqueue(object : Callback<TweetTimeLine> {
                override fun onResponse(
                    call: Call<TweetTimeLine>,
                    response: Response<TweetTimeLine>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                    if (response.body() == null) {
                        failureCount.postValue(failureCount.value?.plus(1) ?: 1)
                        return
                    }
                    if(response.body()!!.isEmpty()){
                        return
                    }
                    val list = timeline.value
                    val range = TweetRange(list!!.size, response.body()!!.size)
                    list.addAll(response.body() as List<Tweet>)
                    timeline.postValue(list)
                    tweetRange.postValue(range)
                }

                override fun onFailure(call: Call<TweetTimeLine>, t: Throwable) {
                    failureCount.postValue(failureCount.value?.plus(1))
                }

            })
    }

    fun onRetweetBtnClicked(id: Long, retweeted: Boolean) {
        if (retweeted) {
            TwitterClient.apiService.createRetweet(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        } else {
            TwitterClient.apiService.destroyRetweet(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }

    }

    fun onLikeBtnClicked(id: Long, liked: Boolean) {
        if (liked) {
            TwitterClient.apiService.createLike(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        } else {
            TwitterClient.apiService.destroyLike(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 429) {
                        tooManyRequests.postValue(true)
                        return
                    }
                    if (response.code().div(100) == 4) {
                        logOut.postValue(true)
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ")
    }

    data class TweetRange(
        val startIndex: Int,
        val count: Int
    )

}