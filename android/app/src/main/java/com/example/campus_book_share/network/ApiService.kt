package com.example.campus_book_share.network

import com.example.campus_book_share.model.LoginResponse
import com.example.campus_book_share.model.PostRequest
import com.example.campus_book_share.model.PostResponse
import com.example.campus_book_share.model.PublishPostResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 定义登录请求的参数结构
data class LoginRequest(val username: String, val password: String)

interface ApiService {
    @POST("api/auth/login") // 对应 Go 后端的路由
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    // 获取图书列表
    // 这里的 "/api/posts" 要和 Go 后端 routes.go 里的地址一致
    @GET("api/posts")
    fun getPosts(): Call<PostResponse>
    // 发布图书
    @POST("api/post/create")
    fun createPost(@Body post: PostRequest): Call<PublishPostResponse>
}