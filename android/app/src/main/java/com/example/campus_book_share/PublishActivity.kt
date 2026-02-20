package com.example.campus_book_share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.campus_book_share.model.PostRequest
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.model.UploadResponse
import com.example.campus_book_share.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PublishActivity : AppCompatActivity() {
    private var coverImageUrl: String = "http://dummyimage.com/200x300"
    private lateinit var ivCoverPreview: ImageView
    private lateinit var btnSelectImage: Button

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).into(ivCoverPreview)
            uploadImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_publish)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.publish)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

            val customTitleView = LayoutInflater.from(this@PublishActivity).inflate(R.layout.action_bar_title, null)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            setCustomView(customTitleView, params)
        }

        val tvTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.action_bar_title)
        tvTitle?.text = "发布图书"

        ivCoverPreview = findViewById(R.id.ivCoverPreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAuthor = findViewById<EditText>(R.id.etAuthor)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val cbSell = findViewById<CheckBox>(R.id.cbSell)
        val etSalePrice = findViewById<EditText>(R.id.etSalePrice)
        val cbRent = findViewById<CheckBox>(R.id.cbRent)
        val etRentPrice = findViewById<EditText>(R.id.etRentPrice)
        val etContact = findViewById<EditText>(R.id.etContact)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        cbSell.setOnCheckedChangeListener { _, isChecked ->
            etSalePrice.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        cbRent.setOnCheckedChangeListener { _, isChecked ->
            etRentPrice.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString()
            val author = etAuthor.text.toString()
            val desc = etDesc.text.toString()
            val contact = etContact.text.toString()

            if (title.isEmpty() || author.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "请补全必填信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbSell.isChecked && !cbRent.isChecked) {
                Toast.makeText(this, "请至少选择一种交易方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val salePrice = if (cbSell.isChecked && etSalePrice.text.isNotEmpty())
                etSalePrice.text.toString().toDouble() else 0.0
            val rentPrice = if (cbRent.isChecked && etRentPrice.text.isNotEmpty())
                etRentPrice.text.toString().toDouble() else 0.0

            val request = PostRequest(
                title = title, author = author, description = desc,
                is_sell = cbSell.isChecked, sale_price = salePrice,
                is_rent = cbRent.isChecked, rent_price = rentPrice,
                contact_wx = contact,
                cover_image = coverImageUrl
            )

            RetrofitClient.apiService.createPost(request).enqueue(object : Callback<PublishPostResponse> {
                override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                    if (response.isSuccessful) {
                        val publishResponse = response.body()
                        if (publishResponse != null && publishResponse.code == 200) {
                            Toast.makeText(this@PublishActivity, publishResponse.msg, Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val errMsg = publishResponse?.msg ?: "发布失败：参数错误"
                            Toast.makeText(this@PublishActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PublishActivity, "发布失败: HTTP${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                    Toast.makeText(this@PublishActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })
        }
    }

    private fun uploadImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp_image.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        RetrofitClient.apiService.uploadImage(body).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    val url = response.body()?.data?.url
                    url?.let {
                        coverImageUrl = it
                        Toast.makeText(this@PublishActivity, "图片上传成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PublishActivity, "图片上传失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Toast.makeText(this@PublishActivity, "图片上传失败: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}