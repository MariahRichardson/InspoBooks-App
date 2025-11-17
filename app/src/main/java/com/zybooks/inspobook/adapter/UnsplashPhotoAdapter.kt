package com.zybooks.inspobook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.UnsplashPhoto

class UnsplashPhotoAdapter(
    private var photos: List<UnsplashPhoto>,
    private val listener: OnPhotoInteractionListener
) : RecyclerView.Adapter<UnsplashPhotoAdapter.PhotoViewHolder>() {

    interface OnPhotoInteractionListener {
        fun onPhotoClicked(photo: UnsplashPhoto)
        fun onPhotoLongClicked(photo: UnsplashPhoto): Boolean
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unsplash_photo, parent, false)
        return PhotoViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        Glide.with(holder.itemView)
            .load(photo.urls.small)
            .centerCrop()
            .into(holder.photoImageView)

        holder.itemView.setOnClickListener {
            listener.onPhotoClicked(photo)
        }

        holder.itemView.setOnLongClickListener {
            listener.onPhotoLongClicked(photo)
        }
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(newList: List<UnsplashPhoto>) {
        photos = newList
        notifyDataSetChanged()
    }

    fun appendPhotos(newList: List<UnsplashPhoto>) {
        val start = photos.size
        photos = photos + newList
        notifyItemRangeInserted(start, newList.size)
    }
}
