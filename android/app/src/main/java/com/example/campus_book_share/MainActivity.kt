package com.example.campus_book_share

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
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
import kotlin.jvm.java

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
        // --- 开始修改 ActionBar ---
        supportActionBar?.apply {
            // 1. 开启“自定义视图”模式
            setDisplayShowCustomEnabled(true)
            // 2. 隐藏系统自带的左对齐标题
            setDisplayShowTitleEnabled(false)
            // 3. 开启返回箭头
            // setDisplayHomeAsUpEnabled(true)

            // 4. 先填充布局，得到 View 对象
            val customTitleView = LayoutInflater.from(this@MainActivity).inflate(R.layout.action_bar_title, null)

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
        tvTitle?.text = "图书广场"
        // --- 修改结束 ---

        // --- 正式代码 ---
        val fabPublish = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPublish)
        fabPublish.setOnClickListener {
            // 跳转到发布页
            startActivity(Intent(this, PublishActivity::class.java))
        }
        // 1. 初始化控件
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) // 绑定控件

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(emptyList()){ postId ->
            // 跳转到详情页
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("POST_ID", postId) // 把 ID 传过去
            startActivity(intent)
        }
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