package com.example.campus_book_share.network

data class CommentRequest(
    val post_id: Int,
    val content: String
)