package com.example.campus_book_share.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_book_share.R
import com.example.campus_book_share.model.Post

class PostAdapter(private var postList: List<Post>,private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // 1. 创建视图 (也就是加载 item_post.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    // 2. 绑定数据 (把 Post 里的数据填到 TextView 里)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvTitle.text = post.title
        holder.tvAuthor.text = post.author

        // 处理价格显示逻辑
        if (post.is_sell) {
            holder.tvSellPrice.visibility = View.VISIBLE
            holder.tvSellPrice.text = "售 ￥${post.sale_price}"
        } else {
            holder.tvSellPrice.visibility = View.GONE
        }

        if (post.is_rent) {
            holder.tvRentPrice.visibility = View.VISIBLE
            holder.tvRentPrice.text = "租 ￥${post.rent_price}"
        } else {
            holder.tvRentPrice.visibility = View.GONE
        }

        // 设置点击监听
        holder.itemView.setOnClickListener {
            // 调用回调函数，把图书ID传出去
            onItemClick(post.ID)
        }
    }

    // 3. 告诉列表有多少条数据
    override fun getItemCount() = postList.size

    // 更新数据的方法
    fun updateData(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }

    // 内部类：持有视图控件
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvSellPrice: TextView = itemView.findViewById(R.id.tvSellPrice)
        val tvRentPrice: TextView = itemView.findViewById(R.id.tvRentPrice)
    }
}