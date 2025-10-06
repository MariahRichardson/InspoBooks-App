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
        var newIBook = InspoBook("Default")
        val updatedList = books.value.orEmpty().toMutableList()
        updatedList.add(newIBook)

        //assignment will trigger MutableLiveData update
        books.value = updatedList
    }

    fun removeBook(book: InspoBook){
        val updatedList = books.value.orEmpty().toMutableList()
        updatedList.remove(book)

        //assignment will trigger MutableLiveData update
        books.value = updatedList
    }

}