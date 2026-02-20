package com.example.campus_book_share.model

data class User(
    val id: Int = 0,
    val username: String = "",
    val student_id: String? = null,
    val contact_wx: String? = null,
    val avatar: String? = null
)