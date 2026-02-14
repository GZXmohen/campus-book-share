package com.example.campus_book_share.model

// 专门对应发布接口的返回结构（data是单个Post对象）
data class PublishPostResponse(
    val code: Int,
    val msg: String,
    val data: Post? // 这里是单个Post对象，不是列表
)
