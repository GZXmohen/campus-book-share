package com.example.campus_book_share

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_book_share.model.Notification
import com.example.campus_book_share.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationActivity : AppCompatActivity() {
    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

            val customTitleView = LayoutInflater.from(this@NotificationActivity).inflate(R.layout.action_bar_title, null)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
            setCustomView(customTitleView, params)
        }

        val tvTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.action_bar_title)
        tvTitle?.text = "通知"

        rvNotifications = findViewById(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(emptyList()) { notificationId ->
            markNotificationAsRead(notificationId)
        }
        rvNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        println("Loading notifications...")
        RetrofitClient.apiService.getNotifications().enqueue(object : Callback<com.example.campus_book_share.network.NotificationListResponse> {
            override fun onResponse(call: Call<com.example.campus_book_share.network.NotificationListResponse>, response: Response<com.example.campus_book_share.network.NotificationListResponse>) {
                println("Notification response code: ${response.code()}")
                println("Notification response body: ${response.body()}")
                if (response.isSuccessful) {
                    val notifications = response.body()?.data ?: emptyList()
                    println("Notifications count: ${notifications.size}")
                    for (notification in notifications) {
                        println("Notification: ${notification.id}, ${notification.content}, ${notification.created_at}")
                    }
                    adapter.updateData(notifications)
                }
            }

            override fun onFailure(call: Call<com.example.campus_book_share.network.NotificationListResponse>, t: Throwable) {
                println("Failed to load notifications: ${t.message}")
            }
        })
    }

    private fun markNotificationAsRead(notificationId: Int) {
        RetrofitClient.apiService.markNotificationAsRead(notificationId).enqueue(object : Callback<com.example.campus_book_share.network.NotificationResponse> {
            override fun onResponse(call: Call<com.example.campus_book_share.network.NotificationResponse>, response: Response<com.example.campus_book_share.network.NotificationResponse>) {
                if (response.isSuccessful) {
                    // 重新加载通知列表
                    loadNotifications()
                }
            }

            override fun onFailure(call: Call<com.example.campus_book_share.network.NotificationResponse>, t: Throwable) {
                println("Failed to mark notification as read: ${t.message}")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class NotificationAdapter(private var notifications: List<Notification>, private val onMarkRead: (Int) -> Unit = {}) : androidx.recyclerview.widget.RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

        class NotificationViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val content: TextView = itemView.findViewById(R.id.tv_notification_content)
            val time: TextView = itemView.findViewById(R.id.tv_notification_time)
            val btnMarkRead: android.widget.Button = itemView.findViewById(R.id.btn_mark_read)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notification = notifications[position]
            holder.content.text = notification.content
            holder.time.text = notification.created_at

            // 根据通知的已读状态设置按钮文本和点击状态
            if (notification.is_read) {
                holder.btnMarkRead.text = "已读"
                holder.btnMarkRead.isEnabled = false
                holder.btnMarkRead.alpha = 0.5f
            } else {
                holder.btnMarkRead.text = "未读"
                holder.btnMarkRead.isEnabled = true
                holder.btnMarkRead.alpha = 1.0f
                
                // 设置标记已读按钮的点击事件
                holder.btnMarkRead.setOnClickListener {
                    onMarkRead(notification.id)
                }
            }

            // 添加点击事件处理
            holder.itemView.setOnClickListener {
                // 检查是否有post_id
                notification.post_id?.let {postId ->
                    if (postId > 0) {
                        // 跳转到详情页
                        val intent = android.content.Intent(holder.itemView.context, DetailActivity::class.java)
                        intent.putExtra("POST_ID", postId)
                        holder.itemView.context.startActivity(intent)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return notifications.size
        }

        fun updateData(newNotifications: List<Notification>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }
    }
}