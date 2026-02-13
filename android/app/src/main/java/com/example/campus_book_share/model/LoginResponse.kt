package com.example.campus_book_share.model

data class LoginResponse(
    val code: Int,
    val msg: String,
    val data: TokenData?
)

data class TokenData(
    val token: String
)
