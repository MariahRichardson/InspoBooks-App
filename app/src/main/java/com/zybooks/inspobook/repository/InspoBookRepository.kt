package com.zybooks.inspobook.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.model.InspoBook
import java.time.LocalDateTime

class InspoBookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val _booksLiveData = MutableLiveData<MutableList<InspoBook>>(mutableListOf())

    val booksLiveData = MutableLiveData<MutableList<InspoBook>>(mutableListOf())

    private fun userBooksCollection() =
        firestore.collection("users").document(auth.currentUser!!.uid).collection("books")

    fun syncBooksFromFirebase() {
        userBooksCollection().addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val fetchedList = snapshot?.toObjects(InspoBook::class.java)?.toMutableList() ?: mutableListOf()

            Log.d("RepoTest", "syncBooksFromFirebase: fetched ${fetchedList.size} books")

            _booksLiveData.value = fetchedList
        }
    }

    fun addBookToFirebase(book: InspoBook) {
        val timeStampOfCreation: Long = System.currentTimeMillis()
        book.id = "book_${timeStampOfCreation}"
        //book.id = "${book.name}_${LocalDateTime.now()}"
        userBooksCollection().document(book.id.toString()).set(book)
            .addOnSuccessListener {
                Log.d("RepoTest", "Book '${book.name}' added successfully")
            }
            .addOnFailureListener {
                Log.e("RepoTest", "Failed to add book: ${it.message}")
            }

    }

    fun updateBookInFirebase(book: InspoBook) {
        userBooksCollection().document(book.id.toString()).set(book, SetOptions.merge())
    }

    fun deleteBooksFromFirebase(toDeleteBooks: List<InspoBook>) {
        val uid = auth.currentUser!!.uid
        val storageRef = FirebaseStorage.getInstance().reference

        for (book in toDeleteBooks) {
            userBooksCollection().document(book.id.toString()).delete()
                .addOnSuccessListener {
                    Log.d("RepositoryTest", "Book '${book.name}' deleted successfully")

                    val bookFolderRef = storageRef.child("users/$uid/books/${book.id}")
                    bookFolderRef.listAll()
                        .addOnSuccessListener { listResult ->
                            val totalFiles = listResult.items.size
                            if (totalFiles == 0) {
                                Log.d("RepositoryTest", "No storage files to delete for ${book.id}")
                                return@addOnSuccessListener
                            }

                            for (fileRef in listResult.items) {
                                fileRef.delete()
                                    .addOnSuccessListener {
                                        Log.d("RepositoryTest", "Deleted ${fileRef.name} from Storage")
                                    }
                                    .addOnFailureListener {
                                        Log.e("RepositoryTest", "Failed to delete ${fileRef.name}: ${it.message}")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Log.e("RepositoryTest", "Failed to list files for ${book.id}: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    Log.e("RepositoryTest", "Failed to delete book '${book.name}': ${it.message}")
                }

            //call delete after deleting all nested data to successfully remove
            userBooksCollection().document(book.id.toString()).delete()
        }
    }


}