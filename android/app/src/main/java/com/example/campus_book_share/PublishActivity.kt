package com.example.campus_book_share

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_book_share.model.PostRequest
import com.example.campus_book_share.model.PostResponse
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PublishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_publish)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.publish)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 绑定控件
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAuthor = findViewById<EditText>(R.id.etAuthor)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val cbSell = findViewById<CheckBox>(R.id.cbSell)
        val etSalePrice = findViewById<EditText>(R.id.etSalePrice)
        val cbRent = findViewById<CheckBox>(R.id.cbRent)
        val etRentPrice = findViewById<EditText>(R.id.etRentPrice)
        val etContact = findViewById<EditText>(R.id.etContact)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // 交互逻辑：勾选才显示价格输入框
        cbSell.setOnCheckedChangeListener { _, isChecked ->
            etSalePrice.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        cbRent.setOnCheckedChangeListener { _, isChecked ->
            etRentPrice.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener {
            // 1. 获取数据
            val title = etTitle.text.toString()
            val author = etAuthor.text.toString()
            val desc = etDesc.text.toString()
            val contact = etContact.text.toString()

            // 2. 简单校验
            if (title.isEmpty() || author.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "请补全必填信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbSell.isChecked && !cbRent.isChecked) {
                Toast.makeText(this, "请至少选择一种交易方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. 构造请求对象
            val salePrice = if (cbSell.isChecked && etSalePrice.text.isNotEmpty())
                etSalePrice.text.toString().toDouble() else 0.0
            val rentPrice = if (cbRent.isChecked && etRentPrice.text.isNotEmpty())
                etRentPrice.text.toString().toDouble() else 0.0

            val request = PostRequest(
                title = title, author = author, description = desc,
                is_sell = cbSell.isChecked, sale_price = salePrice,
                is_rent = cbRent.isChecked, rent_price = rentPrice,
                contact_wx = contact
            )

            // 4. 发送网络请求
            RetrofitClient.apiService.createPost(request).enqueue(object : Callback<PublishPostResponse> {
                override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                    if (response.isSuccessful) {
                        val publishResponse = response.body()
                        // 双重校验：后端返回成功 + 业务码200
                        if (publishResponse != null && publishResponse.code == 200) {
                            Toast.makeText(this@PublishActivity, publishResponse.msg, Toast.LENGTH_SHORT).show()
                            finish() // 关闭页面返回首页
                        } else {
                            // 后端返回了，但业务码不是200（比如参数错误）
                            val errMsg = publishResponse?.msg ?: "发布失败：参数错误"
                            Toast.makeText(this@PublishActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // HTTP状态码不是200（比如404、500）
                        Toast.makeText(this@PublishActivity, "发布失败: HTTP${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                    // 真正的网络错误（比如没网、服务器连不上），打印异常方便调试
                    Toast.makeText(this@PublishActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
                    // 调试用：打印异常信息（能看到具体是解析失败还是真没网）
                    t.printStackTrace()
                }
            })
        }
    }
}