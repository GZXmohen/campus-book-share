package com.example.campus_book_share

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_book_share.model.LoginResponse
import com.example.campus_book_share.network.LoginRequest
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- 开始修改 ActionBar ---
        supportActionBar?.apply {
            // 1. 开启“自定义视图”模式
            setDisplayShowCustomEnabled(true)
            // 2. 隐藏系统自带的左对齐标题
            setDisplayShowTitleEnabled(false)
            // 3. 开启返回箭头
            // setDisplayHomeAsUpEnabled(true)

            // 4. 先填充布局，得到 View 对象
            val customTitleView = LayoutInflater.from(this@LoginActivity).inflate(R.layout.action_bar_title, null)

            // 5. 创建布局参数，并指定 Gravity.CENTER (居中关键！)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )

            // 6. 将 View 对象和布局参数一起设置为自定义视图
            setCustomView(customTitleView, params)
        }

            // 7. 动态修改标题文字 (因为 XML 里写死的是“标题”)
        val tvTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.action_bar_title)
        tvTitle?.text = "登录界面"
            // --- 修改结束 ---

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 发起登录请求
            val request = LoginRequest(username, password)
            RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.code == 200) {
                        val token = response.body()?.data?.token
                        if (token != null) {
                            saveToken(token) // 1. 保存 Token
                            navigateToHome() // 2. 跳转首页
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "登录失败: 账号或密码错误", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvRegister.setOnClickListener {
            // 这里以后做注册页面的跳转
            Toast.makeText(this, "注册功能稍后开发", Toast.LENGTH_SHORT).show()
        }
    }

    // 保存 Token 到 SharedPreferences (安卓自带的轻量级存储)
    private fun saveToken(token: String) {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        sp.edit().putString("token", token).apply()
    }

    // 跳转到主页 (MainActivity)
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 销毁登录页，防止用户按返回键又回到登录页
    }
}