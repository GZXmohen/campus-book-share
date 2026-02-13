package com.example.campus_book_share.model

data class User(
    val id: Int,
    val username: String,
    val student_id: String?,
    val contact_wx: String?,
    val avatar: String?
)
