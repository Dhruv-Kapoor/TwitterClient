package com.example.twitterclient.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.twitterclient.Commons
import com.example.twitterclient.R
import com.example.twitterclient.retrofit.TwitterClient
import kotlinx.android.synthetic.main.splash_screen.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "SplashActivity"

class SplashActivity : AppCompatActivity() {
    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val oauthToken = preferenceManager.getString(OAUTH_TOKEN, "")
        val oauthTokenSecret = preferenceManager.getString(OAUTH_TOKEN_SECRET, "")

        TwitterClient.setToken(oauthToken!!, oauthTokenSecret!!)

        TwitterClient.loginService.verifyLogin().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "onResponse: ${response.code()}")
                if (response.code().div(100) == 2) {
                    startActivity(
                        Intent(this@SplashActivity, MainActivity::class.java),
                        ActivityOptions.makeSceneTransitionAnimation(
                            this@SplashActivity,
                            ivLauncherIcon,
                            getString(R.string.launcherTransition)
                        ).toBundle()
                    )
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Commons.showToast(this@SplashActivity, "Error logging in")
            }

        })
    }
}