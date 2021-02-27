package com.example.twitterclient.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

abstract class TimeLineItem

class TimeLine : ArrayList<TimeLineItem>()
class TweetTimeLine : ArrayList<Tweet>()

data class Tweet(
    val contributors: Any?,
    val coordinates: Any?,
    val created_at: String?,
    val display_text_range: List<Int>?,
    val entities: Entities?,
    val extended_entities: ExtendedEntities?,
    var favorite_count: Int?,
    var favorited: Boolean?,
    var full_text: String?,
    val geo: Any?,
    val id: Long?,
    val id_str: String?,
    val in_reply_to_screen_name: String?,
    val in_reply_to_status_id: Long?,
    val in_reply_to_status_id_str: String?,
    val in_reply_to_user_id: Long?,
    val in_reply_to_user_id_str: String?,
    val is_quote_status: Boolean?,
    val lang: String?,
    val place: Any?,
    val possibly_sensitive: Boolean?,
    val possibly_sensitive_appealable: Boolean?,
    val quoted_status_id: Long?,
    val quoted_status_id_str: String?,
    val quoted_status_permalink: QuotedStatusPermalink?,
    val quoted_status: QuotedStatus?,
    var retweet_count: Int?,
    var retweeted: Boolean?,
    val retweeted_status: Tweet?,
    val source: String?,
    val truncated: Boolean?,
    val user: User?,
) : TimeLineItem()

data class Entities(
    val hashtags: List<Any>?,
    val symbols: List<Any>?,
    val urls: List<Url>?,
    val user_mentions: List<UserMention>?
)

data class ExtendedEntities(
    val media: List<Media>?
)

data class QuotedStatusPermalink(
    val display: String?,
    val expanded: String?,
    val url: String?
)

data class UserMention(
    val id: Long?,
    val id_str: String?,
    val indices: List<Int>?,
    val name: String?,
    val screen_name: String?
)

data class Media(
    val display_url: String?,
    val expanded_url: String?,
    val id: Long?,
    val id_str: String?,
    val indices: List<Int>?,
    val media_url: String?,
    val media_url_https: String?,
    val sizes: Sizes?,
    val type: String?,
    val url: String?,
    val video_info: VideoInfo?
)

data class Sizes(
    val large: Large?,
    val medium: Medium?,
    val small: Small?,
    val thumb: Thumb?
)

data class Large(
    val h: Int?,
    val resize: String?,
    val w: Int?
)

data class Medium(
    val h: Int?,
    val resize: String?,
    val w: Int?
)

data class Small(
    val h: Int?,
    val resize: String?,
    val w: Int?
)

data class Thumb(
    val h: Int?,
    val resize: String?,
    val w: Int?
)

data class QuotedStatus(
    val contributors: Any?,
    val coordinates: Any?,
    val created_at: String?,
    val display_text_range: List<Int>?,
    val entities: Entities?,
    val favorite_count: Int?,
    val favorited: Boolean?,
    val full_text: String?,
    val geo: Any?,
    val id: Long?,
    val id_str: String?,
    val in_reply_to_screen_name: Any?,
    val in_reply_to_status_id: Any?,
    val in_reply_to_status_id_str: Any?,
    val in_reply_to_user_id: Any?,
    val in_reply_to_user_id_str: Any?,
    val is_quote_status: Boolean?,
    val lang: String?,
    val place: Any?,
    val retweet_count: Int?,
    val retweeted: Boolean?,
    val source: String?,
    val truncated: Boolean?,
    val user: User?
)

data class Url(
    val display_url: String?,
    val expanded_url: String?,
    val indices: List<Int>?,
    val url: String?
)

data class User(
    val contributors_enabled: Boolean?,
    val created_at: String?,
    val default_profile: Boolean?,
    val default_profile_image: Boolean?,
    val description: String?,
    val entities: Entities?,
    val favourites_count: Int?,
    val follow_request_sent: Boolean?,
    val followers_count: Int?,
    val following: Boolean?,
    val friends_count: Int?,
    val geo_enabled: Boolean?,
    val has_extended_profile: Boolean?,
    val id: Long?,
    val id_str: String?,
    val is_translation_enabled: Boolean?,
    val is_translator: Boolean?,
    val lang: Any?,
    val listed_count: Int?,
    val location: String?,
    val name: String?,
    val notifications: Boolean?,
    val profile_background_color: String?,
    val profile_background_image_url: String?,
    val profile_background_image_url_https: String?,
    val profile_background_tile: Boolean?,
    val profile_banner_url: String?,
    val profile_image_url: String?,
    val profile_image_url_https: String?,
    val profile_link_color: String?,
    val profile_sidebar_border_color: String?,
    val profile_sidebar_fill_color: String?,
    val profile_text_color: String?,
    val profile_use_background_image: Boolean?,
    val `protected`: Boolean?,
    val screen_name: String?,
    val statuses_count: Int?,
    val time_zone: Any?,
    val translator_type: String?,
    val url: String?,
    val utc_offset: Any?,
    val verified: Boolean?,
    val blocked_by: Boolean?,
    val blocking: Boolean?,
    val live_following: Boolean?,
    val muting: Boolean?
)

data class Hashtag(
    val indices: List<Int>?,
    val text: String?
)

data class Description(
    val urls: List<Any>?
)


data class VideoInfo(
    val aspect_ratio: List<Int>?,
    val duration_millis: Long?,
    val variants: List<Variant>?
)

data class Variant(
    val bitrate: Int?,
    val content_type: String?,
    val url: String?
)

@Parcelize
data class UserParcel(
    val id: Long?,
    val profile_img: String?,
    val banner: String?,
    val name: String?,
    val handle: String?,
    val following: Boolean?,
    val description: String?,
    val followers_count: Int?,
    val friends_count: Int?,
    val link: String?,
    val created_at: String?,
    val profile_link_color: String?
) : Parcelable

@Parcelize
data class MediaParcel(
    val tweetId: Long?,
    val url: String?,
    val bgColor: String?,
    var likeCount: Int?,
    var retweetCount: Int?,
    var retweeted: Boolean?,
    var favorited: Boolean?
) : Parcelable

