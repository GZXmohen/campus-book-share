package com.example.campus_book_share.network

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 定义一个简单的 App 全局 Context 获取器（为了方便拿 SharedPreferences）
@SuppressLint("StaticFieldLeak")
object AppContext {
    lateinit var context: Context
}

object RetrofitClient {
    // 模拟器专用 IP，千万别写 localhost
    private const val BASE_URL = "http://10.0.2.2:8080/"


    // 1. 定义拦截器：自动加 Token
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        // 从 SP 中取出 Token
        val sp =
            AppContext.context.getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        val token = sp.getString("token", "") ?: ""

        // 如果有 Token，就加到 Header 里
        val requestBuilder = original.newBuilder()
        if (token.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 打印具体的请求和响应日志
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor) // 加上 Token 拦截器
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // 绑定日志拦截器
            .build()
            .create(ApiService::class.java)
    }
    // 初始化方法，在 Application 或 MainActivity 里调用一次
    fun init(context: Context) {
        AppContext.context = context.applicationContext
    }
}
