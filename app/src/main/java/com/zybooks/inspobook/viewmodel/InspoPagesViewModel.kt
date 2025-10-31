package com.zybooks.inspobook.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import com.zybooks.inspobook.repository.InspoBookRepository
import com.zybooks.inspobook.repository.InspoPageRepository
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

class InspoPagesViewModel(): ViewModel() {

    // connect to repository
    private val repo = InspoPageRepository()
    private val bookRepo = InspoBookRepository()
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
    var isSyncDone = false
    val WAITPAGEID: String = "temp_wait_page"

    //set an InspoBook object and get its list of pages to display
    fun setupWithBook(book: InspoBook, noPageInit: Bitmap){
        pages.value?.clear()
        selectedBook = book

        // sync pages from firebase
        repo.syncPages(selectedBook.id ?: "Default")

        Log.d("INSPOHH","after repo syncpages in setupWithBook pages size:${pages.value.size} and repo._pagesLiveData size: ${repo._pagesLiveData.value.size}")

        //if book already has pages in firebase
        if(selectedBook.hasPages) {
            //selectedBook.listOfPages = pages.value
            currentPageNum = 0
            if (!pages.value.isNullOrEmpty()) {
                isSyncDone = true
                currentPage.value = pages.value!![0]
            }else{
                //set page to waitpageid
                currentPage.value = InspoPage(WAITPAGEID, noPageInit)
                Log.d("INSPOHH", "Firestore had pages, waiting for syncPages() snapshot...")
            }
        }
        else{
            //if book has no pages, add one default page
            isSyncDone = true
            addPage(noPageInit)
            currentPageNum = 0
            currentPage.value = pages.value!![0]
        }

        //set first page
//        currentPage.value = pages!!.value[0]
        Log.d("INSPOHH","sync ${currentPage.value.pageID} and ${currentPage.value.content} and ${pages.value.size}")
    }

    fun addPage(newPage: Bitmap){
        //once add page is called the hasPages in the select inspobook is set to true
        selectedBook.hasPages = true
        bookRepo.updateBookInFirebase(selectedBook)

        lateinit var newIPage: InspoPage
        newIPage = InspoPage("", newPage)

        val updatedPageList = pages.value.orEmpty().toMutableList()
        updatedPageList.add(newIPage)

        //selectedBook.listOfPages = updatedPageList
        //assignment will trigger MutableLiveData update
        pages.value = updatedPageList

        // add new page to firebase
        repo.addPageToFirebase(selectedBook.id ?: "Default",  newIPage, false)
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
            //selectedBook.listOfPages = updatedList.toMutableList()

            // remove from firebase
            repo.deletePageFromFirebase(selectedBook.id ?: "Default", currentPage.value.pageID)

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
        //only allow update if bitmap is not the wait page(wait used while syncing data)
        if(currentPage.value.pageID != WAITPAGEID) {
            if (isSyncDone) {
                val currentList = pages.value.orEmpty().toMutableList()
                val updatedList = currentList.toMutableList()

                //get page of first id that matches the current page
                Log.d(
                    "INSPOHH",
                    "update ${currentPage.value.pageID} and ${currentPage.value.content} and ${pages.value.size}"
                )
                val indexOfPageToUpdate =
                    currentList.indexOfFirst { it.pageID == currentPage.value.pageID }
                updatedList[indexOfPageToUpdate].content = updatedBitmap

                currentPage.value.content = updatedBitmap
                pages.value = updatedList

                // update firebase
                repo.updatePageInFirebase(
                    selectedBook.id ?: "Default",
                    selectedBook.id ?: "Default",
                    updatedList[indexOfPageToUpdate]
                )
            }
        }
    }


    fun setLoadedPage(inspoPage: InspoPage){
        currentPage.value = inspoPage
    }
    fun getCurrentPageContent(): Bitmap?{
        Log.d("INSPOHH","getCurrentContent ${currentPage.value.pageID} and ${currentPage.value.content}")
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
        //Log.d("fatal inpsopage nextpage","currentPageNum ${currentPageNum} and pages.size ${pages.value.size}")
        //set current page to the next page and increment currentPageNum
        Log.d("INSPOHH", "NEXTPAGE ${pages.value[1].pageID} and ${pages.value[1].content}")
        currentPage.value = pages.value[currentPageNum+1]
        currentPageNum = currentPageNum+1
    }

    fun toPrevPage(){
        //Log.d("fatal inpsopage prevpage","currentPageNum ${currentPageNum} and pages.size ${pages.value.size}")
        //set current page to the previous page and decrement currentPageNum
        Log.d("INSPOHH", "PREVPAGE ${pages.value[0].pageID} and ${pages.value[0].content}")
        currentPage.value = pages.value[currentPageNum-1]
        currentPageNum = currentPageNum-1
    }
}