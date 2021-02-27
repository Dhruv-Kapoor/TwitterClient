package com.example.twitterclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.twitterclient.activities.LoginActivity
import com.example.twitterclient.activities.OAUTH_TOKEN
import com.example.twitterclient.activities.OAUTH_TOKEN_SECRET
import java.util.*


object Commons {
    private const val BILLION = 1000000000.0
    private const val MILLION = 1000000.0
    private const val THOUSAND = 1000.0

    object MilliSeconds{
        const val WEEK = 604800000
        const val DAY = 86400000
        const val HOUR = 3600000
        const val MIN = 60000
        const val SECOND = 1000
    }

    fun logOut(activity: Activity){
        showToast(activity, "Logged Out, Try Again!!")
        ContextCompat.startActivity(activity, Intent(
            activity, LoginActivity::class.java
        ), null)
        val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
        editor.remove(OAUTH_TOKEN)
        editor.remove(OAUTH_TOKEN_SECRET)
        editor.apply()
        activity.finish()
    }

    fun getCountStamp(count: Int?): String? {
        if (count == null) return null
        return when {
            count > BILLION -> {
                String.format("%.2fB", count / BILLION)
            }
            count > MILLION -> {
                String.format("%.2fM", count / MILLION)
            }
            count > THOUSAND -> {
                String.format("%.2fK", count / THOUSAND)
            }
            else -> {
                "$count"
            }
        }
    }

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun getTimeStamp(time: String): String {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
        val dGmt = sdf.parse(time).time
//        val dLocal = dGmt + MilliSeconds.GMTtoIST
        val diff = Date().time - dGmt

        when {
            diff > MilliSeconds.WEEK -> {
                // >1 Week
                return "\u2022 ${SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(dGmt)}"
//                return "\u2022 ${SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(dLocal)}"
            }

            diff > MilliSeconds.DAY -> {
                // >1 Day
                val dayDiff: Int = (diff / MilliSeconds.DAY).toInt()
                return "\u2022 ${dayDiff}d"
            }
            diff > MilliSeconds.HOUR -> {
                // >1 hr
                val hrDiff: Int = (diff / MilliSeconds.HOUR).toInt()
                return "\u2022 ${hrDiff}h"
            }
            diff > MilliSeconds.MIN -> {
                // >1 min
                val minDiff: Int = (diff / MilliSeconds.MIN).toInt()
                return "\u2022 ${minDiff}m"
            }
            else -> {
                val secDiff: Int = (diff / MilliSeconds.SECOND).toInt()
                return "\u2022 ${secDiff}s"
            }
        }
    }
}