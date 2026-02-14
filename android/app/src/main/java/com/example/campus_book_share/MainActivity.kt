package com.example.campus_book_share

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.campus_book_share.adapter.PostAdapter
import com.example.campus_book_share.model.LoginResponse
import com.example.campus_book_share.model.PostResponse
import com.example.campus_book_share.network.LoginRequest
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 正式代码 ---
        // 1. 初始化控件
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) // 绑定控件

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. 设置下拉刷新的监听器
        swipeRefreshLayout.setOnRefreshListener {
            loadPosts() // 一下拉，就重新加载数据
        }

        // 3. 首次进入自动加载
        loadPosts()
    }
    override fun onResume() {
        super.onResume()
        // 每次回到这个页面，都会自动刷新
        loadPosts()
    }
    private fun loadPosts() {
        // 开始请求前，可以让刷新圈圈转起来（可选）
        swipeRefreshLayout.isRefreshing = true

        RetrofitClient.apiService.getPosts().enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                // 请求结束，停止转圈圈
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val posts = response.body()?.data
                    if (posts != null) {
                        // 清空旧数据，换上新数据
                        // 如果要做的更高级，可以用 DiffUtil，但毕设这样足够了
                        adapter.updateData(posts)
                        Toast.makeText(this@MainActivity, "刷新成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                // 请求失败也要停止转圈圈，否则它会一直转
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@MainActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}