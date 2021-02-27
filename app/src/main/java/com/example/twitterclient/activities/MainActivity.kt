package com.example.twitterclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.twitterclient.Commons
import com.example.twitterclient.Commons.getCountStamp
import com.example.twitterclient.Commons.logOut
import com.example.twitterclient.Commons.showToast
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.User
import com.example.twitterclient.fragments.HomeFragment
import com.example.twitterclient.fragments.TrendsFragment
import com.example.twitterclient.retrofit.TwitterClient
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val preferenceManagerEditor by lazy {
        preferenceManager.edit()
    }
    private val actionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
    }
    private val homeFragment by lazy {
        HomeFragment.getInstance()
    }
    private val trendsFragment by lazy {
        TrendsFragment.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        val attachFragment: (Fragment) -> Unit = { frag ->
            supportFragmentManager.beginTransaction().replace(R.id.container, frag).commit()
        }

        attachFragment(homeFragment)

        bottomNavBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.itemHome -> {
                    attachFragment(homeFragment)
                    true
                }
                R.id.itemSearch -> {
                    attachFragment(trendsFragment)
                    true
                }
                R.id.itemNotifications -> {
                    showToast(this, "Not Implemented")
                    false
                }
                R.id.itemMessages -> {
                    showToast(this, "Not Implemented")
                    false
                }
                else -> false
            }
        }
        bottomNavBar.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.itemHome -> {
                    homeFragment.scrollToTop()
                }
            }
        }
        leftNavView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.itemProfile->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemLists->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemTopics->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemBookmarks->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemMoments->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemSettings->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                R.id.itemHelp->{
                    showToast(this@MainActivity, "Not Implemented")
                    true
                }
                else -> false
            }
        }
        ivNightMode.setOnClickListener {
            showToast(this@MainActivity, "Not Implemented")
        }
        ivQr.setOnClickListener {
            showToast(this@MainActivity, "Not Implemented")
        }

        TwitterClient.apiService.verifyCredentials().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.code().div(100) == 4){
                    logOut(this@MainActivity)
                    return
                }
                val user = response.body() as User
                with(leftNavView.getHeaderView(0)) {
                    Log.d(TAG, "onResponse: $user")
                    Picasso.get().load(user.profile_image_url_https?.replace("_normal", ""))
                        .into(this.findViewById<ImageView>(R.id.ivProfilePic))
                    this.findViewById<TextView>(R.id.tvUserName).text = user.name
                    this.findViewById<TextView>(R.id.tvHandle).text = "@${user.screen_name}"
                    with(this.findViewById<TextView>(R.id.tvFollowersCount)) {
                        text = getCountStamp(user.followers_count)
                        setOnClickListener {
                            startActivity(
                                Intent(this@MainActivity, FollowersActivity::class.java)
                            )
                        }
                    }
                    with(this.findViewById<TextView>(R.id.tvFollowingCount)) {
                        text = getCountStamp(user.friends_count)
                        setOnClickListener {
                            startActivity(
                                Intent(this@MainActivity, FollowingActivity::class.java)
                            )
                        }
                    }
                    this.findViewById<TextView>(R.id.tvFollowing).setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, FollowingActivity::class.java)
                        )
                    }
                    this.findViewById<TextView>(R.id.tvFollowers).setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, FollowersActivity::class.java)
                        )
                    }
                }

            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
            }

        })

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun finishAfterTransition() {
        finish()
    }
}