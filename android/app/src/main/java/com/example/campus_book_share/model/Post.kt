package com.example.campus_book_share.model

import com.example.campus_book_share.model.Comment

// 对应后端 GetPostList 接口返回的结构
data class PostResponse(
    val code: Int,
    val msg: String,
    val data: List<Post>
)

data class Post(
    val ID: Int, // 改为大写 ID 以匹配后端 JSON 结构
    val user_id: Int?, // 帖子的用户 ID，改为可空类型
    val title: String,
    val author: String,
    val description: String,
    val is_sell: Boolean,
    val sale_price: Double,
    val is_rent: Boolean,
    val rent_price: Double,
    val cover_image: String?,
    val contact_wx: String?,
    val user: User? = null,
    val comments: List<Comment>? = null
)