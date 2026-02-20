package com.example.campus_book_share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
    private var postId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

            val customTitleView = LayoutInflater.from(this@DetailActivity).inflate(R.layout.action_bar_title, null)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            setCustomView(customTitleView, params)
        }

        val tvTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.action_bar_title)
        tvTitle?.text = "图书详情"

        postId = intent.getIntExtra("POST_ID", -1)
        if (postId == -1) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDetail(postId)

        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            val wx = findViewById<TextView>(R.id.tvContactWx).text.toString().replace("微信号：", "")
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WeChat ID", wx)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEdit)?.setOnClickListener {
            val intent = Intent(this, EditPostActivity::class.java)
            intent.putExtra("POST_ID", postId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
            deletePost()
        }
    }

    private fun loadDetail(id: Int) {
        RetrofitClient.apiService.getPostDetail(id).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val post = response.body()!!.data

                    findViewById<TextView>(R.id.tvDetailTitle).text = post?.title
                    findViewById<TextView>(R.id.tvDetailAuthor).text = "作者：${post?.author}"
                    findViewById<TextView>(R.id.tvDetailDesc).text = post?.description
                    findViewById<TextView>(R.id.tvContactWx).text = "微信号：${post?.contact_wx}"

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

                    findViewById<Button>(R.id.btnEdit)?.visibility = View.VISIBLE
                    findViewById<Button>(R.id.btnDelete)?.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@DetailActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletePost() {
        if (postId == -1) return

        RetrofitClient.apiService.deletePost(postId).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetailActivity, "删除失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}