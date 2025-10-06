package com.zybooks.inspobook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zybooks.inspobook.model.InspoBook

class InspoBooksViewModel : ViewModel() {
    private var books: MutableLiveData<MutableList<InspoBook>> = MutableLiveData<MutableList<InspoBook>>(ArrayList())

    //when someone accesses bookList, give them a read-only version of books
    val bookList: LiveData<MutableList<InspoBook>> get() = books

    //set a new temp variable to make a copy of the current list of InspoBooks, add book and update list of books
    fun addBook(){
        var newIBook = InspoBook("")
        val updatedList = books.value.orEmpty().toMutableList()
        updatedList.add(newIBook)

        //assignment will trigger MutableLiveData update
        books.value = updatedList
    }

    fun removeBooks(toDeleteBooks: List<InspoBook>){
        val currentList = books.value.orEmpty().toMutableList()
        val newList = mutableListOf<InspoBook>()

        //fill newList with all inspobooks that are not selected for deletion
        for(book in currentList){
            if(!toDeleteBooks.contains(book)) {
                newList.add(book)
            }
        }
        //assignment will trigger MutableLiveData update
        books.value = newList
    }

}