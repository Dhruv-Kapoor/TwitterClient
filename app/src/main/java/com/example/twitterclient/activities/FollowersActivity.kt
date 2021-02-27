package com.example.twitterclient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.twitterclient.Commons
import com.example.twitterclient.R
import com.example.twitterclient.adapters.UserAdapterCallbacks
import com.example.twitterclient.adapters.UsersAdapter
import com.example.twitterclient.dataClasses.User
import kotlinx.android.synthetic.main.activity_followers.*
import kotlinx.android.synthetic.main.activity_image_view.toolbar

class FollowersActivity : AppCompatActivity(), UserAdapterCallbacks {

    private val users = ArrayList<User>()
    private val adapter by lazy {
        UsersAdapter(this, users)
    }
    private lateinit var viewModel: FollowersActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Followers"

        rvUsers.adapter = adapter
        rvUsers.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        viewModel = ViewModelProvider(this)[FollowersActivityViewModel::class.java]

        viewModel.getFollowers().observe(this, {
            users.clear()
            users.addAll(it)
        })

        viewModel.getTweetRange().observe(this, {
            adapter.notifyItemRangeChanged(it.startIndex, it.count)
        })
        viewModel.getLogOutStatus().observe(this, {
            if (it) {
                Commons.logOut(this)
            }
        })

    }

    override fun onFollowBtnClicked(id: Long, followed: Boolean) {
        viewModel.updateFollowStatus(id, followed)
    }
}