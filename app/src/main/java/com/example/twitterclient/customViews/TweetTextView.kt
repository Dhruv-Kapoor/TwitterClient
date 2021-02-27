package com.example.twitterclient.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.twitterclient.activities.ProfileActivity
import com.example.twitterclient.activities.USER_KEY
import com.example.twitterclient.dataClasses.User
import com.example.twitterclient.dataClasses.UserParcel
import com.example.twitterclient.retrofit.TwitterClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("AppCompatCustomView")
class TweetTextView(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text == null) {
            super.setText(text, type)
            return
        }
        val spannableString = SpannableString(text)
        var i = 0
        while (i < text.length) {
            if (text[i] == '#') {
                val si = i
                ++i
                while (i < text.length && text[i] != ' ' && text[i] != '\n') {
                    ++i
                }
                spannableString.setSpan(
                    HashTagSpan(context, text.substring(si, i)),
                    si,
                    i,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            else if (text[i] == '@') {
                val si = i
                ++i
                while (i < text.length && text[i] != ' ' && text[i] != '\n') {
                    ++i
                }
                spannableString.setSpan(
                    PersonTagSpan(context, text.substring(si+1, i)),
                    si,
                    i,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            ++i
        }
        super.setText(spannableString, type)
    }

    class HashTagSpan(val context: Context, val hashTag: String) : ClickableSpan() {
        override fun onClick(p0: View) {
            ContextCompat.startActivity(
                context,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://twitter.com/search?q=$hashTag")
                ),
                null
            )
        }
    }

    class PersonTagSpan(val context: Context, val userName: String) : ClickableSpan() {
        override fun onClick(p0: View) {
            TwitterClient.apiService.getUserByName(userName).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    if (!response.isSuccessful || user == null) {
                        return
                    }
                    val userParcel = UserParcel(
                        user.id!!,
                        user.profile_image_url_https,
                        user.profile_banner_url,
                        user.name,
                        user.screen_name,
                        user.following,
                        user.description,
                        user.followers_count,
                        user.favourites_count,
                        user.entities?.urls?.get(0)?.display_url,
                        user.created_at,
                        user.profile_link_color
                    )
                    ContextCompat.startActivity(
                        context,
                        Intent(
                            context,
                            ProfileActivity::class.java
                        ).apply {
                            putExtra(USER_KEY, userParcel)
                        },
                        null
                    )
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    t.printStackTrace()
                }

            })
        }

    }
}