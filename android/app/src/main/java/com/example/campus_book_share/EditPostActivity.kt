package com.example.campus_book_share

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_book_share.model.PostRequest
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPostActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etDesc: EditText
    private lateinit var cbSell: CheckBox
    private lateinit var etSalePrice: EditText
    private lateinit var cbRent: CheckBox
    private lateinit var etRentPrice: EditText
    private lateinit var etContact: EditText
    private lateinit var btnSubmit: Button
    private var postId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)

        supportActionBar?.title = "编辑帖子"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etTitle = findViewById(R.id.etTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etDesc = findViewById(R.id.etDesc)
        cbSell = findViewById(R.id.cbSell)
        etSalePrice = findViewById(R.id.etSalePrice)
        cbRent = findViewById(R.id.cbRent)
        etRentPrice = findViewById(R.id.etRentPrice)
        etContact = findViewById(R.id.etContact)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.text = "保存修改"

        cbSell.setOnCheckedChangeListener { _, isChecked ->
            etSalePrice.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        cbRent.setOnCheckedChangeListener { _, isChecked ->
            etRentPrice.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        postId = intent.getIntExtra("POST_ID", -1)
        if (postId == -1) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPostDetail()

        btnSubmit.setOnClickListener {
            updatePost()
        }
    }

    private fun loadPostDetail() {
        RetrofitClient.apiService.getPostDetail(postId).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val post = response.body()!!.data
                    etTitle.setText(post?.title)
                    etAuthor.setText(post?.author)
                    etDesc.setText(post?.description)
                    etContact.setText(post?.contact_wx)

                    if (post?.is_sell == true) {
                        cbSell.isChecked = true
                        etSalePrice.setText(post.sale_price?.toString() ?: "")
                        etSalePrice.visibility = android.view.View.VISIBLE
                    }

                    if (post?.is_rent == true) {
                        cbRent.isChecked = true
                        etRentPrice.setText(post.rent_price?.toString() ?: "")
                        etRentPrice.visibility = android.view.View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                Toast.makeText(this@EditPostActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePost() {
        val title = etTitle.text.toString()
        val author = etAuthor.text.toString()
        val description = etDesc.text.toString()
        val contactWx = etContact.text.toString()

        val salePrice = etSalePrice.text.toString().toDoubleOrNull() ?: 0.0
        val rentPrice = etRentPrice.text.toString().toDoubleOrNull() ?: 0.0
        val isSell = cbSell.isChecked
        val isRent = cbRent.isChecked

        if (title.isEmpty() || author.isEmpty() || contactWx.isEmpty()) {
            Toast.makeText(this, "请填写必填项", Toast.LENGTH_SHORT).show()
            return
        }

        val postRequest = PostRequest(
            title = title,
            author = author,
            description = description,
            is_sell = isSell,
            sale_price = salePrice,
            is_rent = isRent,
            rent_price = rentPrice,
            contact_wx = contactWx
        )

        RetrofitClient.apiService.updatePost(postId, postRequest).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditPostActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditPostActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                Toast.makeText(this@EditPostActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}