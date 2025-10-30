package com.zybooks.inspobook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.repository.InspoBookRepository

class InspoBooksViewModel : ViewModel() {

    // connect to repository
    private val repo = InspoBookRepository()
    private var books: MutableLiveData<MutableList<InspoBook>> = repo._booksLiveData
    //private var books: MutableLiveData<MutableList<InspoBook>> = MutableLiveData<MutableList<InspoBook>>(ArrayList())

    //when someone accesses bookList, give them a read-only version of books
    val bookList: LiveData<MutableList<InspoBook>> get() = books

    // sync from Firebase when created
    init {
        repo.syncBooksFromFirebase()
    }

    //set a new temp variable to make a copy of the current list of InspoBooks, add book and update list of books
    fun addBook(){
        var newIBook = InspoBook("")
        val updatedList = books.value.orEmpty().toMutableList()
        updatedList.add(newIBook)

        //assignment will trigger MutableLiveData update
        books.value = updatedList

        // add to firebase
        repo.addBookToFirebase(newIBook)
    }

    fun removeBooks(toDeleteBooks: List<InspoBook>){
        val currentList = books.value.orEmpty().toMutableList()
        val updatedList = mutableListOf<InspoBook>()

        //fill newList with all inspobooks that are not selected for deletion
        for(book in currentList){
            if(!toDeleteBooks.contains(book)) {
                updatedList.add(book)
            }
        }
        //assignment will trigger MutableLiveData update
        books.value = updatedList

        // remove from firebase
        repo.deleteBooksFromFirebase(toDeleteBooks)
    }

    fun updateBookName(adjustedBook: InspoBook, newName: String){
        val currentList = books.value.orEmpty().toMutableList()
        val updatedList = currentList.toMutableList()

        //get the index of the item in the list where its id matches some other id
        //it is an object in the list, will go through the whole list if no id match is found
        val indexOfBookToUpdate = currentList.indexOfFirst{it.id == adjustedBook.id}

        //update the copy of the currentList to where the selected inspobook will change its name
        updatedList[indexOfBookToUpdate].name = newName

        //assignment will trigger MutableLiveData update
        books.value = updatedList

        // update firebase
        repo.updateBookInFirebase(updatedList[indexOfBookToUpdate])
    }

}