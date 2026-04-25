package com.example.campus_book_share.network

import com.example.campus_book_share.RegisterRequest
import com.example.campus_book_share.model.Comment
import com.example.campus_book_share.model.LoginResponse
import com.example.campus_book_share.model.Notification
import com.example.campus_book_share.model.PostRequest
import com.example.campus_book_share.model.PostResponse
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.model.SimilarBookResponse
import com.example.campus_book_share.model.UploadResponse
import com.example.campus_book_share.model.User
import com.example.campus_book_share.model.UserResponse
import com.example.campus_book_share.network.CommentRequest
import com.example.campus_book_share.network.CommentResponse
import com.example.campus_book_share.network.CommentListResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// 定义登录请求的参数结构
data class LoginRequest(val username: String, val password: String)

// 定义修改密码请求的参数结构
data class ChangePasswordRequest(val old_password: String, val new_password: String)

// 定义通知响应的结构
data class NotificationResponse(val code: Int, val msg: String, val data: Notification)

// 定义通知列表响应的结构
data class NotificationListResponse(val code: Int, val msg: String, val data: List<Notification>)

interface ApiService {
    @POST("api/auth/login") // 对应 Go 后端的路由
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    // 获取图书列表
    // 这里的 "/api/posts" 要和 Go 后端 routes.go 里的地址一致
    @GET("api/posts")
    fun getPosts(@Query("keyword") keyword: String = ""): Call<PostResponse>
    // 发布图书
    @POST("api/post/create")
    fun createPost(@Body post: PostRequest): Call<PublishPostResponse>
    // 获取详情：GET /api/posts/{id}
    @GET("api/posts/{id}")
    fun getPostDetail(@Path("id") id: Int): Call<PublishPostResponse>
    @GET("api/user/info")
    fun getUserInfo(): Call<UserResponse>

    @PUT("api/user/info")
    fun updateUserInfo(@Body user: User): Call<UserResponse>

    @GET("api/post/my")
    fun getMyPosts(): Call<PostResponse>
    @PUT("api/post/{id}")
    fun updatePost(@Path("id") id: Int, @Body post: PostRequest): Call<PublishPostResponse>
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>
    @DELETE("api/post/{id}")
    fun deletePost(@Path("id") id: Int): Call<PublishPostResponse>
    @Multipart
    @POST("api/upload")
    fun uploadImage(@Part image: MultipartBody.Part): Call<UploadResponse>
    
    // 评论相关接口
    @POST("api/comment/create")
    fun createComment(@Body request: CommentRequest): Call<CommentResponse>
    @GET("api/posts/{id}/comments")
    fun getPostComments(@Path("id") postId: Int): Call<CommentListResponse>
    @DELETE("api/comment/{id}")
    fun deleteComment(@Path("id") commentId: Int): Call<CommentResponse>
    
    // 修改密码
    @POST("api/user/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<UserResponse>
    
    // 通知相关接口
    @GET("api/user/notifications")
    fun getNotifications(): Call<NotificationListResponse>
    @PUT("api/user/notifications/{id}")
    fun markNotificationAsRead(@Path("id") id: Int): Call<NotificationResponse>

    // 相似图书推荐接口
    @GET("api/posts/{id}/similar")
    fun getSimilarBooks(@Path("id") postId: Int, @Query("top_k") topK: Int = 5): Call<SimilarBookResponse>
}