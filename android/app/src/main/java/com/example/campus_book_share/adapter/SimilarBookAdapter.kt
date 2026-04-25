package com.example.campus_book_share.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campus_book_share.R
import com.example.campus_book_share.model.SimilarBook
import com.example.campus_book_share.network.RetrofitClient

class SimilarBookAdapter(
    private var books: List<SimilarBook>,
    private val onItemClick: (SimilarBook) -> Unit
) : RecyclerView.Adapter<SimilarBookAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivSimilarCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvSimilarTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvSimilarAuthor)
        val tvPrice: TextView = itemView.findViewById(R.id.tvSimilarPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_similar_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author

        val priceText = if (book.sale_price > 0) "售 ￥${book.sale_price.toInt()}" else "租 ￥${book.rent_price.toInt()}"
        holder.tvPrice.text = priceText

        val imageUrl = buildCoverImageUrl(book.cover_image)

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.color.teal_200)
                .error(R.color.teal_200)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.color.teal_200)
        }

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    private fun buildCoverImageUrl(coverImage: String?): String? {
        if (coverImage.isNullOrEmpty()) return null
        return if (coverImage.startsWith("http")) {
            coverImage
        } else {
            RetrofitClient.BASE_URL + coverImage.removePrefix("/")
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateData(newBooks: List<SimilarBook>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
