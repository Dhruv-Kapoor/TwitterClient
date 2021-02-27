package com.example.twitterclient.activities

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import com.example.twitterclient.Commons
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.MediaParcel
import com.example.twitterclient.retrofit.TwitterClient
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_gif_view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val GIF_PARCEL = "gif_parcel"

class GifViewActivity : AppCompatActivity() {

    private val gifParcel by lazy {
        intent.getParcelableExtra<MediaParcel>(GIF_PARCEL)
    }
    var player: SimpleExoPlayer? = null
    var playWhenReady = true
    var playbackPosition = 0L
    var currentWindow = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_view)

        if (gifParcel == null) {
            Commons.showToast(this, "Error: Video not found")
            finish()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        tvLikeCount.text = Commons.getCountStamp(gifParcel!!.likeCount)
        tvRetweetCount.text = Commons.getCountStamp(gifParcel!!.retweetCount)

        refreshBottomBar()

        btnRetweet.setOnClickListener {
            onRetweetBtnClicked(
                gifParcel!!.tweetId!!,
                !gifParcel!!.retweeted!!
            )
        }
        btnLike.setOnClickListener {
            onLikeBtnClicked(
                gifParcel!!.tweetId!!,
                !gifParcel!!.favorited!!
            )
        }

        if (savedInstanceState != null) {
            playWhenReady = savedInstanceState.getBoolean(KEY_PLAY_WHEN_READY, true)
            playbackPosition = savedInstanceState.getLong(KEY_PLAYBACK_POSITION, 0)
            currentWindow = savedInstanceState.getInt(KEY_CURRENT_WINDOW, 0)
        }

    }


    private fun refreshBottomBar() {
        if (gifParcel!!.favorited == true) {
            tvLikeCount.setTextColor(getColor(R.color.red))
            btnLike.setColorFilter(getColor(R.color.red))
        } else {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
            tvLikeCount.setTextColor(typedValue.data)
            btnLike.setColorFilter(typedValue.data)
        }
        if (gifParcel!!.retweeted == true) {
            tvRetweetCount.setTextColor(getColor(R.color.green))
            btnRetweet.setColorFilter(getColor(R.color.green))
        } else {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
            tvRetweetCount.setTextColor(typedValue.data)
            btnRetweet.setColorFilter(typedValue.data)
        }
    }

    private fun onRetweetBtnClicked(id: Long, retweeted: Boolean) {
        gifParcel!!.retweeted = retweeted
        if (retweeted) {
            gifParcel!!.retweetCount = gifParcel!!.retweetCount?.plus(1)
        } else {
            gifParcel!!.retweetCount = gifParcel!!.retweetCount?.minus(1)
        }
        refreshBottomBar()

        if (retweeted) {
            TwitterClient.apiService.createRetweet(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@GifViewActivity)
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
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@GifViewActivity)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }
    }

    private fun onLikeBtnClicked(id: Long, liked: Boolean) {
        gifParcel!!.favorited = liked
        if (liked) {
            gifParcel!!.likeCount = gifParcel!!.likeCount?.plus(1)
        } else {
            gifParcel!!.likeCount = gifParcel!!.likeCount?.minus(1)
        }
        refreshBottomBar()
        if (liked) {
            TwitterClient.apiService.createLike(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@GifViewActivity)
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
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@GifViewActivity)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this)
            .build()
        player?.repeatMode = Player.REPEAT_MODE_ONE
        playerView.player = player
        val uri = Uri.parse(gifParcel?.url)
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        buildMediaSource(uri)?.let { player?.prepare(it, false, false) }
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this, "Video Player")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
        outState.putLong(KEY_PLAYBACK_POSITION, playbackPosition)
        outState.putInt(KEY_CURRENT_WINDOW, currentWindow)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    companion object {
        private const val KEY_PLAY_WHEN_READY = "playWhenReady"
        private const val KEY_PLAYBACK_POSITION = "playbackPosition"
        private const val KEY_CURRENT_WINDOW = "currentWindow"
    }
}