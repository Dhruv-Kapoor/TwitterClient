package com.example.twitterclient.activities

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.twitterclient.Commons
import com.example.twitterclient.Commons.getCountStamp
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.UserParcel
import com.example.twitterclient.retrofit.TwitterClient
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

const val USER_KEY = "user"

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = intent.getParcelableExtra<UserParcel>(USER_KEY)
        window.statusBarColor = Color.parseColor("#${user?.profile_link_color?:"FFFFFF"}")
        Picasso.get().load(user?.profile_img?.replace("_normal", "")).into(ivProfilePic)
        Picasso.get().load(user?.banner).into(ivBanner)
        tvUserName.text = user?.name
        tvHandle.text = "@${user?.handle}"
        btnFollow.isChecked = user?.following?:false
        tvDescription.text = user?.description
        tvFollowersCount.text = getCountStamp(user?.followers_count)
        tvFollowingCount.text = getCountStamp(user?.friends_count)
        if(user?.link == null){
            linkLayout.visibility = View.GONE
        }else{
            linkLayout.visibility = View.VISIBLE
            tvLink.text = user.link
        }
        tvJoiningDate.text = getTimeStamp(user?.created_at!!)

        ivBack.setOnClickListener {
            onBackPressed()
        }
        ivMenu.setOnClickListener {
            Toast.makeText(this, "Not Implemented",Toast.LENGTH_SHORT).show()
        }
        btnFollow.setOnCheckedChangeListener{ _: CompoundButton, checked: Boolean ->
            if (checked){
                TwitterClient.apiService.follow(user.id!!).enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code().div(100) == 4){
                            Commons.logOut(this@ProfileActivity)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                })
            }else{
                TwitterClient.apiService.unFollow(user.id!!).enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code().div(100) == 4){
                            Commons.logOut(this@ProfileActivity)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                })
            }
        }

    }

    fun getTimeStamp(time: String): String {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
        val date = sdf.parse(time).time
        return "Joined ${SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(date)}"
    }


}