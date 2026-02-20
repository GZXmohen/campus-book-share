package com.example.campus_book_share.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campus_book_share.R
import com.example.campus_book_share.model.Post
import com.example.campus_book_share.network.RetrofitClient

class PostAdapter(private var postList: List<Post>,private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvTitle.text = post.title
        holder.tvAuthor.text = post.author

        val imageUrl = if (!post.cover_image.isNullOrEmpty()) {
            if (post.cover_image.startsWith("http")) {
                post.cover_image
            } else {
                RetrofitClient.BASE_URL + post.cover_image.removePrefix("/")
            }
        } else {
            null
        }

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.color.teal_200)
        }

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

        holder.itemView.setOnClickListener {
            onItemClick(post.ID)
        }
    }

    override fun getItemCount() = postList.size

    fun updateData(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvSellPrice: TextView = itemView.findViewById(R.id.tvSellPrice)
        val tvRentPrice: TextView = itemView.findViewById(R.id.tvRentPrice)
    }
}