package com.example.campus_book_share.model

import java.io.Serializable

data class Notification(
    val id: Int,
    val user_id: Int,
    val type: Int,
    val content: String,
    val post_id: Int?,
    val is_read: Boolean,
    val created_at: String,
    val user: User,
    val post: Post?
) : Serializable