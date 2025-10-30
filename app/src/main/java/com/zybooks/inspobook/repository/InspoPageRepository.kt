package com.zybooks.inspobook.repository

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import com.zybooks.inspobook.model.User
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class InspoPageRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val _pagesLiveData = MutableLiveData<MutableList<InspoPage>>(mutableListOf())
    val pagesLiveData = MutableLiveData<MutableList<InspoPage>>(mutableListOf())

    private fun pageCollection(bookID: String) =
        firestore.collection("users").document(auth.currentUser!!.uid)
            .collection("books").document(bookID)
            .collection("pages")

    fun syncPages(bookID: String) {
        pageCollection(bookID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("RepoTest", "Error syncing pages: ${e.message}")
                return@addSnapshotListener
            }
            val fetchedList = snapshot?.toObjects(InspoPage::class.java)?.toMutableList() ?: mutableListOf()

            Log.d("RepoTest", "syncPages: fetched ${fetchedList.size} pages for '$bookID'")

            _pagesLiveData.value = fetchedList
        }
    }

    fun isAnyPagesInFirebase(bookID: String, onResult: (Boolean) -> Unit){
        val storageRef = storage.reference
            .child("users/${auth.currentUser!!.uid}/books/$bookID")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val isNotEmpty = listResult.items.isNotEmpty()
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun addPageToFirebase(bookID: String, page: InspoPage, isUpdateFirebase: Boolean) {

        //if the add is not called in the updatePageToFirebase
        if(!isUpdateFirebase) {
//            var formatter: DateTimeFormatter =
//                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS")
//            val timeStampOfCreation: String = LocalDateTime.now().format(formatter)
            val timeStampOfCreation: Long = System.currentTimeMillis()
            page.pageID = "${page.pageID}_${timeStampOfCreation}"
        }
        Log.d("RepoTest", "addPageToFirebase() caled for page ${page.pageID} and ${isUpdateFirebase}")
        val storageRef = storage.reference
            .child("users/${auth.currentUser!!.uid}/books/$bookID/${page.pageID}.png")

        page.content?.let { bmp ->
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            storageRef.putBytes(data).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val pageData = hashMapOf(
                        "pageID" to page.pageID,
                        "imageUrl" to uri.toString()
                    )
                    pageCollection(bookID).document(page.pageID).set(pageData)
                        .addOnSuccessListener {
                            Log.d("RepoTest", "Page '${page.pageID}' added successfully")
                        }
                        .addOnFailureListener {
                            Log.e("RepoTest", "Failed to add page: ${it.message}")
                        }
                }
            }
        }
    }

    fun updatePageInFirebase(bookID: String, ID: String, page: InspoPage) {
        //pageCollection(bookName).document(page.pageID).set(page,SetOptions.merge())
        addPageToFirebase(bookID, page, true)
    }

    fun deletePageFromFirebase(bookID: String, pageID: String) {
        pageCollection(bookID).document(pageID).delete()
        storage.reference.child("users/${auth.currentUser!!.uid}/books/$bookID/$pageID.png").delete()
            .addOnSuccessListener {
                Log.d("RepoTest", "Page '${pageID}' deleted successfully")
            }
            .addOnFailureListener {
                Log.e("RepoTest", "Failed to delete page: ${it.message}")
            }
    }
}