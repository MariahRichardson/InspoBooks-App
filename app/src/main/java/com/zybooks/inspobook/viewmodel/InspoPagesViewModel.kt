package com.zybooks.inspobook.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

class InspoPagesViewModel(): ViewModel() {

    //list of pages to navigate to within a book
    private var pages: MutableLiveData<MutableList<InspoPage>> = MutableLiveData<MutableList<InspoPage>>(ArrayList())
    val pageList: LiveData<MutableList<InspoPage>> get() = pages

    //page being displayed to user and can be edited
    private lateinit var currentPage: MutableLiveData<InspoPage>
    val displayedPage: LiveData<InspoPage> get() = currentPage

    lateinit var selectedBook: InspoBook

    //set an InspoBook object and get its list of pages to display
    fun setupWithBook(book: InspoBook){
        selectedBook = book
        if(book.listOfPages.isNotEmpty()) {
            pages.value = book.listOfPages.orEmpty().toMutableList()
        }
        else{
            //if book has no pages, add one default page
            addPage()
        }
    }

    fun addPage(){
        var newIPage = InspoPage("${selectedBook.name}")
        val updatedPageList = pages.value.orEmpty().toMutableList()
        updatedPageList.add(newIPage)

        //assignment will trigger MutableLiveData update
        pages.value = updatedPageList
    }

    fun removePage(){
    }

    fun savePage(bitmap: Bitmap){
    }
}