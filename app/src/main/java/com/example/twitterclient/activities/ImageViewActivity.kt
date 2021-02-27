package com.example.twitterclient.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.twitterclient.Commons
import com.example.twitterclient.Commons.getCountStamp
import com.example.twitterclient.Commons.showToast
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.MediaParcel
import com.example.twitterclient.retrofit.TwitterClient
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val IMAGE_PARCEL = "image_url"
const val IMAGE_TRANSITION_NAME = "image_transition"

class ImageViewActivity : AppCompatActivity() {
    private val imageParcel by lazy {
        intent.getParcelableExtra<MediaParcel>(IMAGE_PARCEL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        if (imageParcel == null) {
            showToast(this, "Error: Image not found")
            finish()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        Picasso.get().load(imageParcel!!.url).into(iv)

        tvLikeCount.text = getCountStamp(imageParcel!!.likeCount)
        tvRetweetCount.text = getCountStamp(imageParcel!!.retweetCount)

        refreshBottomBar()

        btnRetweet.setOnClickListener {
            onRetweetBtnClicked(
                imageParcel!!.tweetId!!,
                !imageParcel!!.retweeted!!
            )
        }
        btnLike.setOnClickListener {
            onLikeBtnClicked(
                imageParcel!!.tweetId!!,
                !imageParcel!!.favorited!!
            )
        }
    }

    fun refreshBottomBar() {
        if (imageParcel!!.favorited == true) {
            tvLikeCount.setTextColor(getColor(R.color.red))
            btnLike.setColorFilter(getColor(R.color.red))
        } else {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
            tvLikeCount.setTextColor(typedValue.data)
            btnLike.setColorFilter(typedValue.data)
        }
        if (imageParcel!!.retweeted == true) {
            tvRetweetCount.setTextColor(getColor(R.color.green))
            btnRetweet.setColorFilter(getColor(R.color.green))
        } else {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
            tvRetweetCount.setTextColor(typedValue.data)
            btnRetweet.setColorFilter(typedValue.data)
        }
    }

    fun onRetweetBtnClicked(id: Long, retweeted: Boolean) {
        imageParcel!!.retweeted = retweeted
        if (retweeted) {
            imageParcel!!.retweetCount = imageParcel!!.retweetCount?.plus(1)
        } else {
            imageParcel!!.retweetCount = imageParcel!!.retweetCount?.minus(1)
        }
        refreshBottomBar()

        if (retweeted) {
            TwitterClient.apiService.createRetweet(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@ImageViewActivity)
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
                        Commons.logOut(this@ImageViewActivity)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }
    }

    fun onLikeBtnClicked(id: Long, liked: Boolean) {
        imageParcel!!.favorited = liked
        if (liked) {
            imageParcel!!.likeCount = imageParcel!!.likeCount?.plus(1)
        } else {
            imageParcel!!.likeCount = imageParcel!!.likeCount?.minus(1)
        }
        refreshBottomBar()
        if (liked) {
            TwitterClient.apiService.createLike(id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code().div(100) == 4){
                        Commons.logOut(this@ImageViewActivity)
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
                        Commons.logOut(this@ImageViewActivity)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}

            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_view_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemSave -> {
                showToast(this, "Not Implemented")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}