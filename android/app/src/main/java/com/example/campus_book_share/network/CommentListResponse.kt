package com.example.campus_book_share.network

import com.example.campus_book_share.model.Comment

data class CommentListResponse(
    val code: Int,
    val msg: String,
    val data: List<Comment>
)