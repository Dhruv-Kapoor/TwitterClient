package com.example.twitterclient.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.twitterclient.Commons
import com.example.twitterclient.Commons.showToast
import com.example.twitterclient.R
import com.example.twitterclient.activities.*
import com.example.twitterclient.adapters.TimeLineAdapter
import com.example.twitterclient.adapters.TimeLineAdapterCallbacks
import com.example.twitterclient.dataClasses.*
import kotlinx.android.synthetic.main.fragment_home.*

private const val TAG = "HomeFragment"
private const val FIRST_TWEET_ID = "firstTweetId"
private const val SCROLL_POSITION = "scrollPosition"

class HomeFragment : Fragment(), TimeLineAdapterCallbacks, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private var INSTANCE: HomeFragment? = null
        fun getInstance(): HomeFragment {
            val tempInstance = INSTANCE
            return if (tempInstance != null) {
                tempInstance
            } else {
                val t = HomeFragment()
                INSTANCE = t
                t
            }
        }
    }

    private val timeLine = TimeLine()
    private lateinit var layoutManager: LinearLayoutManager
    private val adapter by lazy {
        TimeLineAdapter(this, timeLine)
    }
    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val preferenceManagerEditor by lazy {
        preferenceManager.edit()
    }

    private var viewModel: HomeFragmentViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this.requireActivity()).get(HomeFragmentViewModel::class.java)
        viewModel?.firstTweetId = preferenceManager.getLong(FIRST_TWEET_ID, -1)

        layoutManager = LinearLayoutManager(context)
        rvTimeline.layoutManager = layoutManager
        rvTimeline.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        swipeRefreshLayout.setOnRefreshListener(this)
        rvTimeline.adapter = adapter

        viewModel?.init(
            (preferenceManager.getInt(SCROLL_POSITION, 0) + 5).coerceAtLeast(
                DEFAULT_TWEET_COUNT
            )
        )

        viewModel?.getScrollPosUpdate()?.observe(viewLifecycleOwner, {
            if (it) {
                rvTimeline.scrollToPosition(preferenceManager.getInt(SCROLL_POSITION, 0))
                viewModel?.loadLatestTweets()
            }
        })

        viewModel?.getFailureCount()?.observe(viewLifecycleOwner, Observer {
            if (it > 0)
                showToast(requireContext(), "Failed to load tweets")
        })

        viewModel?.getTimeLine()?.observe(viewLifecycleOwner, Observer {
            timeLine.clear()
//            if(it.size>0)
//                timeLine.add(it[13])
            timeLine.addAll(it)
        })

        viewModel?.getTweetRange()?.observe(viewLifecycleOwner, Observer { tweetRange ->
            adapter.notifyItemRangeChanged(tweetRange.startIndex, tweetRange.count)
        })

        viewModel?.refreshStatus()?.observe(viewLifecycleOwner, {
            swipeRefreshLayout.isRefreshing = it
        })
        viewModel?.getLogOutStatus()?.observe(viewLifecycleOwner, {
            if (it) {
                Commons.logOut(requireActivity())
            }
        })
        viewModel?.getTooManyRequestsStatus()?.observe(viewLifecycleOwner, {
            if(it){
                showToast(requireContext(), "Too Many Requests, Wait 15 Minutes")
            }
        })
//        rvTimeline.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                var p = layoutManager.findFirstCompletelyVisibleItemPosition()
//                if (p == -1) {
//                    p = layoutManager.findFirstVisibleItemPosition()
//                }
//                viewModel?.scrollPosition = p
////                Log.d(TAG, "onScrolled: ${viewModel?.scrollPosition}")
//            }
//        })

    }

    override fun onDestroyView() {
        preferenceManagerEditor.putLong(FIRST_TWEET_ID, viewModel?.firstTweetId!!)
        preferenceManagerEditor.putInt(SCROLL_POSITION, viewModel?.scrollPosition!!)
        preferenceManagerEditor.commit()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        viewModel?.let {
            it.scrollPosition = layoutManager.findFirstVisibleItemPosition()
            Log.d(TAG, "onPause: ${it.scrollPosition}")
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onTimeLineEndReached() {
        val lastTweetId = (timeLine[timeLine.lastIndex] as Tweet).id!!
        Log.d(TAG, "onTimeLineEndReached: $lastTweetId")
        viewModel?.loadOldTweets(lastTweetId)
    }

    override fun onLinkPreviewClicked(url: String) {
        Log.d(TAG, "onLinkPreviewClicked: $url")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onImageClicked(tweet: Tweet, url: String, imageView: ImageView) {
        Log.d(TAG, "onImageClicked: $url")
        val imageParcel = MediaParcel(
            tweet.id,
            url,
            null,
            tweet.favorite_count,
            tweet.retweet_count,
            tweet.retweeted,
            tweet.favorited
        )
        imageView.transitionName = IMAGE_TRANSITION_NAME
        startActivity(
            Intent(context, ImageViewActivity::class.java).apply {
                putExtra(IMAGE_PARCEL, imageParcel)
            },
            ActivityOptions.makeSceneTransitionAnimation(activity, imageView, IMAGE_TRANSITION_NAME)
                .toBundle()
        )
    }

    override fun onRetweetBtnClicked(position: Int, id: Long, retweeted: Boolean) {
        val tweet = (timeLine[position] as Tweet)
        tweet.retweeted = retweeted
        if (retweeted) {
            tweet.retweet_count = tweet.retweet_count?.plus(1)
        } else {
            tweet.retweet_count = tweet.retweet_count?.minus(1)
        }
        adapter.notifyItemChanged(position)
        viewModel?.onRetweetBtnClicked(id, retweeted)
    }

    override fun onLikeBtnClicked(position: Int, id: Long, liked: Boolean) {
        val tweet = (timeLine[position] as Tweet)
        tweet.favorited = liked
        if (liked) {
            tweet.favorite_count = tweet.favorite_count?.plus(1)
        } else {
            tweet.favorite_count = tweet.favorite_count?.minus(1)
        }
        adapter.notifyItemChanged(position)
        viewModel?.onLikeBtnClicked(id, liked)
    }

    override fun onVideoClicked(tweet: Tweet, videoInfo: VideoInfo) {
//        var url = ""
//        for(variant in videoInfo.variants!!){
//            if(variant.bitrate == 0){
//                url = variant.url!!
//            }
//        }
        val videoParcel = MediaParcel(
            tweet.id,
            videoInfo.variants!![0].url,
            null,
            tweet.favorite_count,
            tweet.retweet_count,
            tweet.retweeted,
            tweet.favorited
        )
        startActivity(
            Intent(context, VideoViewActivity::class.java).apply {
                putExtra(VIDEO_PARCEL, videoParcel)
            }
        )
    }

    override fun onGifClicked(tweet: Tweet, videoInfo: VideoInfo) {
        val gifParcel = MediaParcel(
            tweet.id,
            videoInfo.variants!![0].url,
            null,
            tweet.favorite_count,
            tweet.retweet_count,
            tweet.retweeted,
            tweet.favorited
        )
        startActivity(
            Intent(context, GifViewActivity::class.java).apply {
                putExtra(GIF_PARCEL, gifParcel)
            }
        )
    }

    override fun onProfileClicked(user: User) {
        val userParcel = UserParcel(
            user.id,
            user.profile_image_url_https,
            user.profile_banner_url,
            user.name,
            user.screen_name,
            user.following,
            user.description,
            user.followers_count,
            user.friends_count,
            user.entities?.urls?.get(0)?.display_url,
            user.created_at,
            user.profile_link_color
        )
        val intent = Intent(context, ProfileActivity::class.java)
        intent.putExtra(USER_KEY, userParcel)
        startActivity(intent)
    }

    override fun onRefresh() {
        viewModel?.refresh()
    }

    fun scrollToTop() {
        rvTimeline.smoothScrollToPosition(0)
    }

}