package com.example.campus_book_share.model

data class UserResponse(
    val code: Int,
    val msg: String,
    val data: User?
)