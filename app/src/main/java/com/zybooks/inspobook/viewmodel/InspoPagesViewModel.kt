package com.zybooks.inspobook.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
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
    private var currentPage: MutableLiveData<InspoPage> = MutableLiveData<InspoPage>()
    val displayedPage: LiveData<InspoPage> get() = currentPage

    lateinit var selectedBook: InspoBook

    //set an InspoBook object and get its list of pages to display
    fun setupWithBook(book: InspoBook, noPageInit: Bitmap){
        pages.value?.clear()
        selectedBook = book
        if(book.listOfPages.isNotEmpty()) {
            pages.value = book.listOfPages.toMutableList()
        }
        else{
            //if book has no pages, add one default page
            addPage(noPageInit)
        }

        //set first page
        currentPage.value = pages!!.value[0]
    }

    fun addPage(newPage: Bitmap){
        var newIPage = InspoPage("${selectedBook.name}", newPage)
        val updatedPageList = pages.value.orEmpty().toMutableList()
        updatedPageList.add(newIPage)

        selectedBook.listOfPages = updatedPageList
        //assignment will trigger MutableLiveData update
        pages.value = updatedPageList
    }

    fun removePage(){
    }

    fun updatePage(updatedBitmap: Bitmap?){
        //update the bitmap of the current InspoPage
        val currentList = pages.value.orEmpty().toMutableList()
        val updatedList = currentList.toMutableList()

        //get page of first id that matches the current page
        val indexOfPageToUpdate = currentList.indexOfFirst{it.pageID == currentPage.value.pageID}
        updatedList[indexOfPageToUpdate].content = updatedBitmap

        currentPage.value.content = updatedBitmap
        pages.value = updatedList
    }

    fun getCurrentPageContent(): Bitmap?{
        return currentPage.value.content
    }
}