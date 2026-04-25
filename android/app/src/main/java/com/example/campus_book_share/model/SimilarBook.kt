package com.example.campus_book_share.model

data class SimilarBook(
    val book_id: Int,
    val similarity: Double,
    val title: String,
    val author: String,
    val cover_image: String?,
    val sale_price: Double,
    val rent_price: Double
)

data class SimilarBookResponse(
    val code: Int,
    val msg: String,
    val data: List<SimilarBook>?
)
