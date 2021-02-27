package com.example.twitterclient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.twitterclient.R
import com.example.twitterclient.dataClasses.Trend
import kotlinx.android.synthetic.main.trend_item_view.view.*

class TrendsAdapter(
    private val trendsAdapterCallbacks: TrendsAdapterCallbacks,
    private val list: List<Trend>
) : RecyclerView.Adapter<TrendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        return TrendViewHolder(
            trendsAdapterCallbacks,
            LayoutInflater.from(parent.context).inflate(R.layout.trend_item_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        holder.bind(position, list[position])
    }

    override fun getItemCount(): Int = list.size
}

class TrendViewHolder(private val trendsAdapterCallbacks: TrendsAdapterCallbacks, itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    fun bind(position: Int, trend: Trend) {
        with(itemView) {

            tvHeader.text = "${position + 1} \u2022 Trending"
            tvTrendName.text = trend.name
            val t = getTrendVolume(trend.tweet_volume)
            if (t == null) {
                tvVolume.visibility = View.GONE
            } else {
                tvVolume.text = t
            }
            setOnClickListener {
                trendsAdapterCallbacks.onTrendClicked(trend.url ?: "")
            }
        }
    }

    private fun getTrendVolume(tweetVolume: Int?): String? {
        if (tweetVolume == null) return null
        return when {
            tweetVolume > BILLION -> {
                String.format("%.2fB Tweets", tweetVolume / BILLION)
            }
            tweetVolume > MILLION -> {
                String.format("%.2fM Tweets", tweetVolume / MILLION)
            }
            tweetVolume > THOUSAND -> {
                String.format("%.2fK Tweets", tweetVolume / THOUSAND)
            }
            else -> {
                "$tweetVolume Tweets"
            }
        }
    }

    companion object {
        private const val BILLION = 1000000000.0
        private const val MILLION = 1000000.0
        private const val THOUSAND = 1000.0
    }
}

interface TrendsAdapterCallbacks {
    fun onTrendClicked(url: String)
}