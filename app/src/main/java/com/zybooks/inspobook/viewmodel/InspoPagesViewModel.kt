package com.zybooks.inspobook.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import com.zybooks.inspobook.repository.InspoPageRepository
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

class InspoPagesViewModel(): ViewModel() {

    // connect to repository
    private val repo = InspoPageRepository()
    private var pages: MutableLiveData<MutableList<InspoPage>> = repo._pagesLiveData

    //list of pages to navigate to within a book
    //private var pages: MutableLiveData<MutableList<InspoPage>> = MutableLiveData<MutableList<InspoPage>>(ArrayList())
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

        // sync pages from firebase
        repo.syncPages(selectedBook.name ?: "Default")

        //if(book.listOfPages.isNotEmpty()) {
        if(pages.value.isNotEmpty()) {
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
            newIPage = InspoPage("${selectedBook.name}_0", newPage)
        }
        else {
            //if there is an existing page, new page id is <bookname>_<#ofcurrentpages+1>
            newIPage = InspoPage("${selectedBook.name}_${pages.value.size + 1}", newPage)
        }

        val updatedPageList = pages.value.orEmpty().toMutableList()
        updatedPageList.add(newIPage)

        selectedBook.listOfPages = updatedPageList
        //assignment will trigger MutableLiveData update
        pages.value = updatedPageList

        // add new page to firebase
        repo.addPageToFirebase(selectedBook.name ?: "Default", newIPage)
    }

    //return true if page is removed, return false otherwise
    fun removePage() : Boolean{
        //delete the current page the user is on if the number of pages is more than 1
        if(pages.value.size > 1){
            val currentList = pages.value.orEmpty().toMutableList()

            //remove all pages with the same id as the current page id
            val updatedList = currentList.filter{it.pageID != currentPage.value.pageID}

            //assignment will trigger MutableLiveData update, keep pages update to date as well as the inspobook's list of books
            pages.value = updatedList.toMutableList()
            selectedBook.listOfPages = updatedList.toMutableList()

            // remove from firebase
            repo.deletePageFromFirebase(selectedBook.name ?: "Default", currentPage.value.pageID)

            //if a previous pages exist set current page to that after removing page
            if(doesPreviousPageExist()){
                toPrevPage()
            }
            else{
                //since there is initially 2 of more pages, if a previous page does not exist then move to next page(which is the same current page as it is deleted)
                currentPage.value = pages.value[currentPageNum]
            }

            return true
        }
        else{
            //if there is only one page(or less) and user wants to delete, return false
            return false
        }
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

        // update firebase
        repo.updatePageInFirebase(selectedBook.name ?: "Default", updatedList[indexOfPageToUpdate])
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
        Log.d("fatal inpsopage nextpage","currentPageNum ${currentPageNum} and pages.size ${pages.value.size}")
        //set current page to the next page and increment currentPageNum
        currentPage.value = pages.value[currentPageNum+1]
        currentPageNum = currentPageNum+1
    }

    fun toPrevPage(){
        Log.d("fatal inpsopage prevpage","currentPageNum ${currentPageNum} and pages.size ${pages.value.size}")
        //set current page to the previous page and decrement currentPageNum
        currentPage.value = pages.value[currentPageNum-1]
        currentPageNum = currentPageNum-1
    }
}