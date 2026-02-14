package com.example.campus_book_share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail)) { v, insets ->
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
            setDisplayHomeAsUpEnabled(true)

            // 4. 先填充布局，得到 View 对象
            val customTitleView = LayoutInflater.from(this@DetailActivity).inflate(R.layout.action_bar_title, null)

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
        tvTitle?.text = "图书详情"
        // --- 修改结束 ---

        // 1. 获取传递过来的 ID
        val postId = intent.getIntExtra("POST_ID", -1)
        if (postId == -1) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. 加载数据
        loadDetail(postId)

        // 3. 设置复制按钮逻辑
        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            val wx = findViewById<TextView>(R.id.tvContactWx).text.toString().replace("微信号：", "")
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WeChat ID", wx)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDetail(id: Int) {
        RetrofitClient.apiService.getPostDetail(id).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val post = response.body()!!.data

                    // 填充数据
                    findViewById<TextView>(R.id.tvDetailTitle).text = post?.title
                    findViewById<TextView>(R.id.tvDetailAuthor).text = "作者：${post?.author}"
                    findViewById<TextView>(R.id.tvDetailDesc).text = post?.description
                    findViewById<TextView>(R.id.tvContactWx).text = "微信号：${post?.contact_wx}"

                    // 价格显示逻辑
                    val tvSale = findViewById<TextView>(R.id.tvDetailSalePrice)
                    if (post != null) {
                        if (post.is_sell) {
                            tvSale.visibility = View.VISIBLE
                            tvSale.text = "售 ￥${post.sale_price}"
                        }
                    }
                    val tvRent = findViewById<TextView>(R.id.tvDetailRentPrice)
                    if (post != null) {
                        if (post.is_rent) {
                            tvRent.visibility = View.VISIBLE
                            tvRent.text = "租 ￥${post.rent_price}"
                        }
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // 处理点击
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}