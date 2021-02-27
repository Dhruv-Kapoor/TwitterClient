package com.example.twitterclient.dataClasses

data class UsersResponse(
    val users: List<User>?,
    val next_cursor: Long?,
    val next_cursor_str: String?,
    val previous_cursor: Long?,
    val previous_cursor_str: String?,
    val total_count: Long?
)