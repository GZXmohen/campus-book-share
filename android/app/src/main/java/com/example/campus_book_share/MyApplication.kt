package com.example.campus_book_share

import android.app.Application
import com.example.campus_book_share.network.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 在这里初始化RetrofitClient
        // 这样整个APP运行期间都能使用
        RetrofitClient.init(this)
    }
}