package com.example.campus_book_share.model

data class UploadResponse(
    val code: Int,
    val msg: String,
    val data: UploadData?
)

data class UploadData(
    val url: String
)