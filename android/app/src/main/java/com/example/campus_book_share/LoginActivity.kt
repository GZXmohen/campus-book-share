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

        if (isLoggedIn()) {
            navigateToHome()
            return
        }

        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)

            val customTitleView = LayoutInflater.from(this@LoginActivity).inflate(R.layout.action_bar_title, null)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            setCustomView(customTitleView, params)
        }

        val tvTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.action_bar_title)
        tvTitle?.text = "登录界面"

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        val savedUsername = getSavedUsername()
        if (savedUsername.isNotEmpty()) {
            etUsername.setText(savedUsername)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(username, password)
            RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.code == 200) {
                        val token = response.body()?.data?.token
                        if (token != null) {
                            saveToken(token)
                            saveUsername(username)
                            navigateToHome()
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
            Toast.makeText(this, "注册功能稍后开发", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLoggedIn(): Boolean {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        val token = sp.getString("token", "")
        return !token.isNullOrEmpty()
    }

    private fun saveToken(token: String) {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        sp.edit().putString("token", token).apply()
    }

    private fun saveUsername(username: String) {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        sp.edit().putString("username", username).apply()
    }

    private fun getSavedUsername(): String {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        return sp.getString("username", "") ?: ""
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}