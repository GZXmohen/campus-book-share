package com.example.campus_book_share

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_book_share.adapter.PostAdapter
import com.example.campus_book_share.model.PostResponse
import com.example.campus_book_share.model.User
import com.example.campus_book_share.model.UserResponse
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var etStudentId: EditText
    private lateinit var etContactWx: EditText
    private lateinit var btnSave: Button
    private lateinit var rvMyPosts: RecyclerView
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "个人中心"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvUsername = findViewById(R.id.tvUsername)
        etStudentId = findViewById(R.id.etStudentId)
        etContactWx = findViewById(R.id.etContactWx)
        btnSave = findViewById(R.id.btnSave)
        rvMyPosts = findViewById(R.id.rvMyPosts)

        rvMyPosts.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(emptyList()) { postId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("POST_ID", postId)
            startActivity(intent)
        }
        rvMyPosts.adapter = adapter

        loadUserInfo()
        loadMyPosts()

        btnSave.setOnClickListener {
            updateUserInfo()
        }
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadUserInfo() {
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    user?.let {
                        tvUsername.text = it.username
                        etStudentId.setText(it.student_id ?: "")
                        etContactWx.setText(it.contact_wx ?: "")
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "加载失败: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserInfo() {
        val user = User(
            student_id = etStudentId.text.toString(),
            contact_wx = etContactWx.text.toString()
        )
        RetrofitClient.apiService.updateUserInfo(user).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "保存成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMyPosts() {
        RetrofitClient.apiService.getMyPosts().enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    val posts = response.body()?.data
                    posts?.let { adapter.updateData(it) }
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadMyPosts()
    }
    private fun logout() {
        val sp = getSharedPreferences("book_share_data", Context.MODE_PRIVATE)
        sp.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}