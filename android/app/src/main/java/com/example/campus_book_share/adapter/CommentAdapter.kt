package com.example.campus_book_share.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_book_share.R
import com.example.campus_book_share.model.Comment

class CommentAdapter(
    private var comments: List<Comment>,
    private val currentUserId: Int,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tv_comment_username)
        val content: TextView = itemView.findViewById(R.id.tv_comment_content)
        val time: TextView = itemView.findViewById(R.id.tv_comment_time)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.username.text = comment.user.username
        holder.content.text = comment.content
        holder.time.text = comment.created_at

        // 只有评论作者才能看到删除按钮
        if (comment.user_id == currentUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(comment.id)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun updateData(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}