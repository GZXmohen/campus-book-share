package com.example.campus_book_share

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_book_share.model.LoginResponse
import com.example.campus_book_share.network.LoginRequest
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 测试代码开始 ---
        // 这里的用户名密码换成你数据库里已有的
        val request = LoginRequest("mohen_test", "12345678")

        RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("TestNetwork", "登录成功！Token: ${body?.data?.token}")
                } else {
                    Log.e("TestNetwork", "登录失败: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("TestNetwork", "连接错误: ${t.message}")
            }
        })
        // --- 测试代码结束 ---
    }
}