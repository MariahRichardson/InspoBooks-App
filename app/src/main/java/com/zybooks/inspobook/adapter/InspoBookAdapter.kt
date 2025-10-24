package com.zybooks.inspobook.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.InspoBook

class InspoBookAdapter(private var inspoBooks: List<InspoBook>, private var listenerItemClick: OnItemClickListener) :
    RecyclerView.Adapter<InspoBookAdapter.ViewHolder>() {

    //get inspobook item when item click listener is triggered
    interface OnItemClickListener{
        fun onItemClick(item: InspoBook)
    }
    var isSelectMode: Boolean = false
    //private var selectedPositions = mutableSetOf<Int>()
    private var selectedBooks = mutableSetOf<InspoBook>()

    //call when list of books change, will notify the recyclerview to also update
    fun updateInspoBooks(newBooks: List<InspoBook>){
        inspoBooks = newBooks
        clearAllSelections()
    }

    fun getSelectedItems(): List<InspoBook> = selectedBooks.toList()

    //viewholder will contain the view of one item of the list that recyclerview will display
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        //get view from single_inspo_book xml
        val titleView: TextView = view.findViewById(R.id.inspoBookName)
        val bookCoverView: ImageView = view.findViewById(R.id.inspoBookCoverPage)



        init{
            //apply listeners here

            view.setOnClickListener {
                //get position of item
                val pos = bindingAdapterPosition

                if(isSelectMode && pos != RecyclerView.NO_POSITION){
                    toggleSelection(inspoBooks[pos])
                }
                else if(!isSelectMode && pos != RecyclerView.NO_POSITION){
                    //TODO: navigate to book pages if clicked
                    var inspoBookSelected = inspoBooks[pos]
                    Log.d("InspoBookAdapter","$pos positioni book clicked!")

                    //once viewholder is clicked with select mode off,
                    listenerItemClick.onItemClick(inspoBookSelected)
                }

            }

        }
    }

    //a ViewHolder will hold a single book's view, recyclerView will create as many to fill the screen when applicable
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("InspoBookAdapter", "onCreateViewHolder() called")
        //get the single_inspo_book xml to display a single inspo book for the viewholder
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.single_inspo_book, viewGroup, false)
        return ViewHolder(view)
    }

    //bind an certain item from the list of inspobooks to a single viewholder
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //TODO: adjust implementation for cover images when user changes it
        viewHolder.titleView.text = inspoBooks[position].name

        //set cover page to some value
        viewHolder.bookCoverView.setImageResource(R.drawable.default_inspobook_cover)

        val book = inspoBooks[position]
        //set background color of viewholder with a selected item to gray if item is selected else set to nothing
        viewHolder.itemView.setBackgroundColor(
            if(selectedBooks.contains(book)) {
                ContextCompat.getColor(viewHolder.itemView.getContext(),R.color.light_gray)}
            else{
                Color.TRANSPARENT}
        )

    }

    //selection will be removed it previously selected else place the position of the viewholder selected

    fun toggleSelection(position: InspoBook){
        if(selectedBooks.contains(position)){
            selectedBooks.remove(position)
        }
        else{
            selectedBooks.add(position)
        }
        notifyDataSetChanged()
    }

    //remove all selections
    fun clearAllSelections(){
        selectedBooks.clear()
        isSelectMode = false
        notifyDataSetChanged()
    }

    override fun getItemCount() = inspoBooks.size
}