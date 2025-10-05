package com.zybooks.inspobook.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.InspoBook

class InspoBookAdapter :
    RecyclerView.Adapter<InspoBookAdapter.ViewHolder>() {
    private var inspoBooks: List<InspoBook> = listOf()

    fun setInspoBooks(newBooks: List<InspoBook>){
        inspoBooks = newBooks
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        //get view from single_inspo_book xml
        val titleView: TextView = view.findViewById(R.id.inspoBookName)
        //val bookCoverView: ImageView = view.findViewById(R.id.inspoBookCoverImage)

        init{
            //apply listeners here
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("InspoBookAdapter", "onCreateViewHolder() called")
        //get the single_inspo_book xml to display a single inspo book for the viewholder
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.single_inspo_book, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //TODO: add implementation for cover images
        viewHolder.titleView.text = inspoBooks[position].bookName
        //viewHolder.imageView = inspoBooks[position].
    }

    override fun getItemCount() = inspoBooks.size
}