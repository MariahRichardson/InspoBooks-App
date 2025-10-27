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

    //track the current page number of the inspobook, first page in inspobook is page number 0
    var currentPageNum = 0
    lateinit var selectedBook: InspoBook

    //set an InspoBook object and get its list of pages to display
    fun setupWithBook(book: InspoBook, noPageInit: Bitmap){
        pages.value?.clear()
        selectedBook = book
        if(book.listOfPages.isNotEmpty()) {
            pages.value = book.listOfPages.toMutableList()
            currentPageNum = 0
        }
        else{
            //if book has no pages, add one default page
            addPage(noPageInit)
            currentPageNum = 0
        }

        //set first page
        currentPage.value = pages!!.value[0]
    }

    fun addPage(newPage: Bitmap){
        lateinit var newIPage: InspoPage
        if(pages.value.size < 1) {
            //if the list of existing pages in the book is non-existent, first page is the <bookname>_0
            newIPage = InspoPage("${selectedBook.name}_0", newPage) }
        else {
            //if there is an existing page, new page id is <bookname>_<#ofcurrentpages+1>
            newIPage = InspoPage("${selectedBook.name}_${pages.value.size + 1}", newPage) }

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

    fun doesPreviousPageExist(): Boolean{
        //return true a previous page exists, else false
        if(currentPageNum > 0)
            return true
        return false
    }

    fun doesNextPageExist(): Boolean{
        //return true if a next page exists, else false
        if(currentPageNum+1 < pages.value.size)
            return true
        return false
    }

    fun toNextPage(){
        //set current page to the next page and increment currentPageNum
        currentPage.value = pages.value[currentPageNum+1]
        currentPageNum = currentPageNum+1
    }

    fun toPrevPage(){
        //set current page to the previous page and decrement currentPageNum
        currentPage.value = pages.value[currentPageNum-1]
        currentPageNum = currentPageNum-1
    }
}