package com.example.campus_book_share.model

import java.io.Serializable

data class Comment(
    val id: Int,
    val post_id: Int,
    val user_id: Int,
    val content: String,
    val created_at: String,
    val user: User
) : Serializable