package com.example.campus_book_share.model

data class PostRequest(
    val title: String,
    val author: String,
    val description: String,
    val is_sell: Boolean,
    val sale_price: Double,
    val is_rent: Boolean,
    val rent_price: Double,
    val contact_wx: String,
    // cover_image 暂时先留空或者写死一个 URL
    val cover_image: String = "http://dummyimage.com/200x300"
)
