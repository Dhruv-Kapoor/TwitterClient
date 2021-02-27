package com.example.twitterclient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_item_view.view.*

class UsersAdapter(val userAdapterCallbacks: UserAdapterCallbacks, val list: List<User>) :
    RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            userAdapterCallbacks,
            LayoutInflater.from(parent.context).inflate(R.layout.user_item_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}

class UserViewHolder(val userAdapterCallbacks: UserAdapterCallbacks, itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    fun bind(user: User) {
        with(itemView){
            tvUserName.text = user.name
            tvHandle.text = "@${user.screen_name}"
            tvDescription.text = user.description
            Picasso.get().load(user.profile_image_url_https?.replace("_normal","")).into(ivProfilePic)
            btnFollow.isChecked = user.following?:false
            btnFollow.setOnCheckedChangeListener { _, checked ->
                userAdapterCallbacks.onFollowBtnClicked(user.id!!, checked)
            }
        }
    }
}

interface UserAdapterCallbacks {
    fun onFollowBtnClicked(id: Long, followed: Boolean)
}