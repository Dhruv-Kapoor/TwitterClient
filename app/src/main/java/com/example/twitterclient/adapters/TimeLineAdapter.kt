package com.example.twitterclient.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.twitterclient.Commons
import com.example.twitterclient.R
import com.example.twitterclient.adapters.TimeLineAdapter.Companion.EXCEPTION_URLS
import com.example.twitterclient.adapters.TimeLineAdapter.Companion.GIF
import com.example.twitterclient.adapters.TimeLineAdapter.Companion.PHOTO
import com.example.twitterclient.adapters.TimeLineAdapter.Companion.VIDEO
import com.example.twitterclient.dataClasses.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.tweet_item_view.view.*
import kotlinx.android.synthetic.main.tweet_item_view.view.iv1
import kotlinx.android.synthetic.main.tweet_no_media.view.ivProfilePic
import kotlinx.android.synthetic.main.tweet_no_media.view.retweetTextLayout
import kotlinx.android.synthetic.main.tweet_no_media.view.tvContent
import kotlinx.android.synthetic.main.tweet_no_media.view.tvHandle
import kotlinx.android.synthetic.main.tweet_no_media.view.tvRetweet
import kotlinx.android.synthetic.main.tweet_no_media.view.tvTime
import kotlinx.android.synthetic.main.tweet_no_media.view.tvUserName
import kotlinx.android.synthetic.main.tweet_video.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class TimeLineAdapter(
    private val timeLineAdapterCallbacks: TimeLineAdapterCallbacks,
    private val list: List<TimeLineItem>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflate =
            { layout: Int -> LayoutInflater.from(parent.context).inflate(layout, parent, false) }

        return when (viewType) {
            TWEET_NO_MEDIA -> TweetNoMediaViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_no_media)
            )
            TWEET_ONE_IMAGE -> TweetOneImageViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_one_image)
            )
            TWEET_TWO_IMAGES -> TweetTwoImagesViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_two_images)
            )
            TWEET_THREE_IMAGES -> TweetThreeImagesViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_three_images)
            )
            TWEET_FOUR_IMAGES -> TweetFourImagesViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_four_images)
            )
            TWEET_LINK_PREVIEW -> TweetLinkPreviewViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_link_preview)
            )
            TWEET_VIDEO -> TweetVideoViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_video)
            )
            TWEET_GIF -> TweetGifViewHolder(
                timeLineAdapterCallbacks,
                inflate(R.layout.tweet_video)
            )
            else -> EmptyViewHolder(
                inflate(R.layout.empty_view)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TweetViewHolder -> {
                val tweet = list[position] as Tweet
                if (tweet.retweeted_status != null) {
                    holder.setHeaders(tweet.retweeted_status, tweet.user?.name)
                    holder.bind(tweet.retweeted_status)
                } else {
                    holder.setHeaders(tweet, null)
                    holder.bind(tweet)
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        var tweet = list[position]
        when (tweet) {
            is Tweet -> {
                if (tweet.retweeted_status != null) {
                    tweet = tweet.retweeted_status!! as Tweet
                }
                if(tweet.is_quote_status == true){
                    tweet.full_text = tweet.full_text?.replace(tweet.quoted_status_permalink?.url!!,"")
                }
                var imageCount = 0
                tweet.extended_entities?.media?.forEach {
                    when (it.type) {
                        PHOTO -> {
                            imageCount++
                        }
                        VIDEO -> {
                            return TWEET_VIDEO
                        }
                        GIF -> {
                            return TWEET_GIF
                        }
                    }
                }
                when (imageCount) {
                    1 -> return TWEET_ONE_IMAGE
                    2 -> return TWEET_TWO_IMAGES
                    3 -> return TWEET_THREE_IMAGES
                    4 -> return TWEET_FOUR_IMAGES
                }

                if (tweet.entities?.urls?.isNullOrEmpty() != true) {
                    for (url in tweet.entities?.urls!!) {
                        val eUrl = URL(url.expanded_url)
                        if (!EXCEPTION_URLS.contains(eUrl.host)) {
                            return TWEET_LINK_PREVIEW
                        }
                    }
                }

                return TWEET_NO_MEDIA

            }
        }
        return UNSUPPORTED
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.adapterPosition > list.size - 5) {
            timeLineAdapterCallbacks.onTimeLineEndReached()
        }
    }

    companion object {
        //TimLine Entities
        const val UNSUPPORTED = -1
        const val TWEET_NO_MEDIA = 1
        const val TWEET_ONE_IMAGE = 2
        const val TWEET_TWO_IMAGES = 3
        const val TWEET_THREE_IMAGES = 4
        const val TWEET_FOUR_IMAGES = 5
        const val TWEET_LINK_PREVIEW = 6
        const val TWEET_VIDEO = 7
        const val TWEET_GIF = 8

        //Media Types
        const val PHOTO = "photo"
        const val VIDEO = "video"
        const val GIF = "animated_gif"

        val EXCEPTION_URLS = HashSet<String>(10).apply {
            add("youtube.com")
            add("www.youtube.com")
            add("youtu.be")
            add("t.co")
            add("twitter.com")
        }
    }
}

abstract class TweetViewHolder(
    val timeLineAdapterCallbacks: TimeLineAdapterCallbacks,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(tweet: Tweet)
    fun setHeaders(tweet: Tweet, retweetingUser: String?) {
        with(itemView) {
            if (retweetingUser != null) {
                retweetTextLayout.visibility = VISIBLE
                tvRetweet.text = "$retweetingUser retweeted"
            } else {
                retweetTextLayout.visibility = GONE
            }

            Picasso.get().load(tweet.user?.profile_image_url_https?.replace("_normal", ""))
                .into(ivProfilePic)
            tvUserName.text = tweet.user?.name
            tvHandle.text = "@${tweet.user?.screen_name}"
            tvTime.text = Commons.getTimeStamp(tweet.created_at!!)

            ivProfilePic.setOnClickListener {
                timeLineAdapterCallbacks.onProfileClicked(tweet.user!!)
            }
        }
    }

    fun setBottomBar(tweet: Tweet) {
        with(itemView) {
            tvLikeCount.text = Commons.getCountStamp(tweet.favorite_count)
            tvRetweetCount.text = Commons.getCountStamp(tweet.retweet_count)
            if (tweet.favorited == true) {
                tvLikeCount.setTextColor(context.getColor(R.color.red))
                btnLike.setColorFilter(context.getColor(R.color.red))
            } else {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
                tvLikeCount.setTextColor(typedValue.data)
                btnLike.setColorFilter(typedValue.data)
            }
            if (tweet.retweeted == true) {
                tvRetweetCount.setTextColor(context.getColor(R.color.green))
                btnRetweet.setColorFilter(context.getColor(R.color.green))
            } else {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true)
                tvRetweetCount.setTextColor(typedValue.data)
                btnRetweet.setColorFilter(typedValue.data)
            }
            btnRetweet.setOnClickListener {
                timeLineAdapterCallbacks.onRetweetBtnClicked(
                    adapterPosition,
                    tweet.id!!,
                    !tweet.retweeted!!
                )
            }
            btnLike.setOnClickListener {
                timeLineAdapterCallbacks.onLikeBtnClicked(
                    adapterPosition,
                    tweet.id!!,
                    !tweet.favorited!!
                )
            }
        }
    }
}

class TweetNoMediaViewHolder(timeLineAdapterCallbacks: TimeLineAdapterCallbacks, itemView: View) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        with(itemView) {
            tvContent.text = content
        }
        setBottomBar(tweet)
    }
}

class TweetOneImageViewHolder(timeLineAdapterCallbacks: TimeLineAdapterCallbacks, itemView: View) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        val photoUrls = ArrayList<String>()
        for (media in tweet.extended_entities?.media!!) {
            if (media.type == PHOTO) {
                photoUrls.add(media.media_url_https!!)
                content = content?.replace(media.url!!, "")
                break
            }
        }
//        if (tweet.is_quote_status == true) {
//            content = content?.replace(
//                tweet.quoted_status_permalink?.url!!,
//                "\n${tweet.quoted_status_permalink.expanded!!}"
//            )
//        }
        with(itemView) {
            tvContent.text = content
            val imageViews = arrayOf(iv1)
            photoUrls.forEachIndexed { index, url ->
                Picasso.get().load(url).into(imageViews[index])
                imageViews[index].setOnClickListener {
                    timeLineAdapterCallbacks.onImageClicked(tweet, url, imageViews[index])
                }
            }

        }
        setBottomBar(tweet)
    }
}

class TweetTwoImagesViewHolder(timeLineAdapterCallbacks: TimeLineAdapterCallbacks, itemView: View) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        val photoUrls = ArrayList<String>()
        for (media in tweet.extended_entities?.media!!) {
            if (media.type == PHOTO) {
                photoUrls.add(media.media_url_https!!)
                content = content?.replace(media.url!!, "")
            }
        }
//        if (tweet.is_quote_status == true) {
//            content = content?.replace(
//                tweet.quoted_status_permalink?.url!!,
//                "\n${tweet.quoted_status_permalink.expanded!!}"
//            )
//        }
        with(itemView) {
            tvContent.text = content
            val imageViews = arrayOf(iv1, iv2)
            photoUrls.forEachIndexed { index, url ->
                Picasso.get().load(url).into(imageViews[index])
                imageViews[index].setOnClickListener {
                    timeLineAdapterCallbacks.onImageClicked(tweet, url, imageViews[index])
                }
            }
        }
        setBottomBar(tweet)
    }
}

class TweetThreeImagesViewHolder(
    timeLineAdapterCallbacks: TimeLineAdapterCallbacks,
    itemView: View
) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        val photoUrls = ArrayList<String>()
        for (media in tweet.extended_entities?.media!!) {
            if (media.type == PHOTO) {
                photoUrls.add(media.media_url_https!!)
                content = content?.replace(media.url!!, "")
            }
        }
//        if (tweet.is_quote_status == true) {
//            content = content?.replace(
//                tweet.quoted_status_permalink?.url!!,
//                "\n${tweet.quoted_status_permalink.expanded!!}"
//            )
//        }
        with(itemView) {
            tvContent.text = content
            val imageViews = arrayOf(iv1, iv2, iv3)
            photoUrls.forEachIndexed { index, url ->
                Picasso.get().load(url).into(imageViews[index])
                imageViews[index].setOnClickListener {
                    timeLineAdapterCallbacks.onImageClicked(tweet, url, imageViews[index])
                }
            }
        }
        setBottomBar(tweet)
    }
}

class TweetFourImagesViewHolder(
    timeLineAdapterCallbacks: TimeLineAdapterCallbacks,
    itemView: View
) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        val photoUrls = ArrayList<String>()
        for (media in tweet.extended_entities?.media!!) {
            if (media.type == PHOTO) {
                photoUrls.add(media.media_url_https!!)
                content = content?.replace(media.url!!, "")
            }
        }
//        if (tweet.is_quote_status == true) {
//            content = content?.replace(
//                tweet.quoted_status_permalink?.url!!,
//                "\n${tweet.quoted_status_permalink.expanded!!}"
//            )
//        }
        with(itemView) {
            tvContent.text = content
            val imageViews = arrayOf(iv1, iv2, iv3, iv4)
            photoUrls.forEachIndexed { index, url ->
                Picasso.get().load(url).into(imageViews[index])
                imageViews[index].setOnClickListener {
                    timeLineAdapterCallbacks.onImageClicked(tweet, url, imageViews[index])
                }
            }
        }
        setBottomBar(tweet)
    }
}

class TweetLinkPreviewViewHolder(
    timeLineAdapterCallbacks: TimeLineAdapterCallbacks,
    itemView: View
) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        var url: Url? = null
        for (t in tweet.entities?.urls!!) {
            if (!EXCEPTION_URLS.contains(url)) {
                url = t
                content = content?.replace(t.url!!, "")
                continue
            }
            content = content?.replace(t.url!!, t.display_url!!)
        }
//        if (tweet.is_quote_status == true) {
//            content = content?.replace(
//                tweet.quoted_status_permalink?.url!!,
//                "\n${tweet.quoted_status_permalink.expanded!!}"
//            )
//        }
        with(itemView) {
            tvContent.text = content
            setOnClickListener {
                timeLineAdapterCallbacks.onLinkPreviewClicked(url?.expanded_url!!)
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val jsoup = Jsoup.connect(url?.expanded_url)
                val doc = jsoup.get()
                val title = doc.title()
                val image = doc.select("img[src~=(?i)\\.(png|jpe?g)]")[0].attr("src")

                withContext(Dispatchers.Main) {
                    with(itemView) {
                        Picasso.get().load(image).into(ivLinkPreview)
                        tvLinkTitle.text = title
                        tvDisplayUrl.text = url?.display_url?.split("/")?.get(0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setBottomBar(tweet)
    }
}

class TweetVideoViewHolder(timeLineAdapterCallbacks: TimeLineAdapterCallbacks, itemView: View) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        var media: Media? = null
        for (it in tweet.extended_entities?.media!!) {
            if (it.type == VIDEO) {
                media = it
                break
            }
        }
        content = content?.replace(media?.url!!, "")
        with(itemView) {
            tvContent.text = content
            Picasso.get().load(media?.media_url_https).into(iv1)
            val durationSeconds = media?.video_info?.duration_millis?.div(1000)?.toInt()
//            val duration = "${}:${}"
            val duration = String.format("%d:%02d", durationSeconds?.div(60), durationSeconds?.rem(60))
            tvStamp.text = duration
            iv1.setOnClickListener {
                timeLineAdapterCallbacks.onVideoClicked(tweet, media?.video_info!!)
            }
        }
        setBottomBar(tweet)
    }
}

class TweetGifViewHolder(timeLineAdapterCallbacks: TimeLineAdapterCallbacks, itemView: View) :
    TweetViewHolder(timeLineAdapterCallbacks, itemView) {
    override fun bind(tweet: Tweet) {
        var content = tweet.full_text
        if (!tweet.entities?.urls.isNullOrEmpty())
            for (url in tweet.entities?.urls!!) {
                content = content?.replace(url.url!!, url.display_url!!)
            }
        var media: Media? = null
        for (it in tweet.extended_entities?.media!!) {
            if (it.type == GIF) {
                media = it
                break
            }
        }
        content = content?.replace(media?.url!!, "")
        with(itemView) {
            tvContent.text = content
            Picasso.get().load(media?.media_url_https).into(iv1)
            tvStamp.text = "GIF"
            iv1.setOnClickListener {
                timeLineAdapterCallbacks.onGifClicked(tweet, media?.video_info!!)
            }
        }
        setBottomBar(tweet)
    }
}

class EmptyViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView)

interface TimeLineAdapterCallbacks {
    fun onTimeLineEndReached()
    fun onLinkPreviewClicked(url: String)
    fun onImageClicked(tweet: Tweet, url: String, imageView: ImageView)
    fun onRetweetBtnClicked(position: Int, id: Long, retweeted: Boolean)
    fun onLikeBtnClicked(position: Int, id: Long, liked: Boolean)
    fun onProfileClicked(user: User)
    fun onVideoClicked(tweet: Tweet, videoInfo: VideoInfo)
    fun onGifClicked(tweet: Tweet, videoInfo: VideoInfo)
}