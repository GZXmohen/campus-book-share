package com.example.campus_book_share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campus_book_share.adapter.CommentAdapter
import com.example.campus_book_share.model.Comment
import com.example.campus_book_share.model.PublishPostResponse
import com.example.campus_book_share.model.User
import com.example.campus_book_share.model.UserResponse
import com.example.campus_book_share.network.CommentRequest
import com.example.campus_book_share.network.CommentResponse
import com.example.campus_book_share.network.CommentListResponse
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class DetailActivity : AppCompatActivity() {
    private var postId: Int = -1
    private var currentUserId: Int? = null // 改为可空类型
    private lateinit var rvComments: RecyclerView
    private lateinit var tvNoComments: TextView
    private lateinit var etComment: EditText
    private lateinit var btnSendComment: Button
    private lateinit var commentAdapter: CommentAdapter

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
        if (postId == -1 || postId == 0) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 加载当前用户信息
        loadUserInfo()

        // 初始化评论相关组件
        rvComments = findViewById(R.id.rvComments)
        tvNoComments = findViewById(R.id.tvNoComments)
        etComment = findViewById(R.id.etComment)
        btnSendComment = findViewById(R.id.btnSendComment)

        // 初始化评论列表
        rvComments.layoutManager = LinearLayoutManager(this)
        // 先创建一个空的adapter，等用户信息加载完成后再更新
        commentAdapter = CommentAdapter(emptyList(), currentUserId ?: -1) { commentId ->
            deleteComment(commentId)
        }
        rvComments.adapter = commentAdapter

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

        // 发送评论按钮点击事件
        btnSendComment.setOnClickListener {
            sendComment()
        }
    }

    private fun loadDetail(id: Int) {
        println("Loading post detail with ID: $id")
        RetrofitClient.apiService.getPostDetail(id).enqueue(object : Callback<PublishPostResponse> {
            override fun onResponse(call: Call<PublishPostResponse>, response: Response<PublishPostResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val post = response.body()!!.data

                    // 打印完整的帖子数据
                    println("Complete post data: $post")
                    println("Response body: ${response.body()}")

                    findViewById<TextView>(R.id.tvDetailTitle).text = post?.title
                    findViewById<TextView>(R.id.tvDetailAuthor).text = "作者：${post?.author}"
                    findViewById<TextView>(R.id.tvDetailDesc).text = post?.description
                    findViewById<TextView>(R.id.tvContactWx).text = "微信号：${post?.contact_wx}"

                    val ivCover = findViewById<ImageView>(R.id.ivDetailCover)
                    val imageUrl = if (!post?.cover_image.isNullOrEmpty()) {
                        if (post?.cover_image?.startsWith("http") == true) {
                            post.cover_image
                        } else {
                            RetrofitClient.BASE_URL + post?.cover_image?.removePrefix("/")
                        }
                    } else {
                        null
                    }

                    if (imageUrl != null) {
                        Glide.with(this@DetailActivity).load(imageUrl).into(ivCover)
                    } else {
                        ivCover.setImageResource(R.color.teal_200)
                    }

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

                    // 只有帖子作者才能看到编辑和删除按钮
                    val btnEdit = findViewById<Button>(R.id.btnEdit)
                    val btnDelete = findViewById<Button>(R.id.btnDelete)
                    println("btnEdit: $btnEdit")
                    println("btnDelete: $btnDelete")
                    
                    // 打印用户 ID 和帖子作者 ID
                    println("Current user ID: $currentUserId")
                    println("Post user_id: ${post?.user_id}")
                    println("Post user: ${post?.user}")
                    println("Post user.id: ${post?.user?.id}")
                    
                    // 尝试多种方式获取帖子作者 ID
                    var postAuthorId: Int? = null
                    if (post?.user_id != null) {
                        postAuthorId = post.user_id
                        println("Using post.user_id: $postAuthorId")
                    } else if (post?.user?.id != null) {
                        postAuthorId = post.user.id
                        println("Using post.user.id: $postAuthorId")
                    } else {
                        println("Unable to get post author ID")
                    }
                    
                    println("Final post author ID: $postAuthorId")
                    println("Current user ID: $currentUserId")
                    println("Are they equal? ${postAuthorId == currentUserId}")
                    
                    // 只有帖子作者才能看到编辑和删除按钮
                    if (currentUserId != null && postAuthorId == currentUserId) {
                        println("User is the author, showing edit/delete buttons")
                        btnEdit?.visibility = View.VISIBLE
                        btnDelete?.visibility = View.VISIBLE
                    } else {
                        println("User is not the author, hiding edit/delete buttons")
                        btnEdit?.visibility = View.GONE
                        btnDelete?.visibility = View.GONE
                    }

                    // 加载评论列表
                    loadComments()
                } else {
                    println("Failed to load post, response: ${response.body()}")
                    Toast.makeText(this@DetailActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublishPostResponse>, t: Throwable) {
                println("Failed to load post, error: ${t.message}")
                Toast.makeText(this@DetailActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadComments() {
        RetrofitClient.apiService.getPostComments(postId).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(call: Call<CommentListResponse>, response: Response<CommentListResponse>) {
                if (response.isSuccessful) {
                    val comments = response.body()?.data ?: emptyList()
                    if (comments.isEmpty()) {
                        tvNoComments.visibility = View.VISIBLE
                        rvComments.visibility = View.GONE
                    } else {
                        tvNoComments.visibility = View.GONE
                        rvComments.visibility = View.VISIBLE
                        // 更新现有adapter的数据，而不是创建新实例
                        commentAdapter.updateData(comments)
                    }
                }
            }

            override fun onFailure(call: Call<CommentListResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "加载评论失败: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserInfo() {
        println("Loading user info...")
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                println("User info response: ${response.body()}")
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    println("User data: $user")
                    user?.let {
                        currentUserId = it.id
                        println("User info loaded successfully, user ID: $currentUserId")
                        // 更新评论适配器的用户ID
                        commentAdapter = CommentAdapter(emptyList(), currentUserId ?: -1) {
                            deleteComment(it)
                        }
                        rvComments.adapter = commentAdapter
                        // 加载帖子详情
                        loadDetail(postId)
                    }
                } else {
                    println("Failed to load user info, response code: ${response.code()}")
                    println("Response error: ${response.errorBody()?.string()}")
                    // 即使获取用户信息失败，也加载帖子详情
                    loadDetail(postId)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                println("Failed to load user info, error: ${t.message}")
                // 即使获取用户信息失败，也加载帖子详情
                loadDetail(postId)
            }
        })
    }

    private fun sendComment() {
        val content = etComment.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CommentRequest(postId, content)
        RetrofitClient.apiService.createComment(request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailActivity, "评论成功", Toast.LENGTH_SHORT).show()
                    etComment.text.clear()
                    // 重新加载评论列表
                    loadComments()
                } else {
                    Toast.makeText(this@DetailActivity, "评论失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "网络错误", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteComment(commentId: Int) {
        println("Deleting comment with ID: $commentId")
        println("Current user ID: $currentUserId")
        RetrofitClient.apiService.deleteComment(commentId).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                println("Delete comment response: ${response.body()}")
                println("Delete comment response code: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                println("Delete comment error body: $errorBody")
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailActivity, "删除评论成功", Toast.LENGTH_SHORT).show()
                    // 重新加载评论列表
                    loadComments()
                } else {
                    println("Delete comment error: $errorBody")
                    val errorMessage = when (response.code()) {
                        401 -> "请先登录"
                        403 -> "无权删除此评论"
                        404 -> "评论不存在"
                        else -> "删除评论失败: $errorBody"
                    }
                    Toast.makeText(this@DetailActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                println("Delete comment failure: ${t.message}")
                Toast.makeText(this@DetailActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
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