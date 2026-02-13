package com.example.campus_book_share.network

import com.example.campus_book_share.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// 定义登录请求的参数结构
data class LoginRequest(val username: String, val password: String)

interface ApiService {
    @POST("api/auth/login") // 对应 Go 后端的路由
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}