package com.zybooks.inspobook.repository

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import java.io.ByteArrayOutputStream


class InspoPageRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val _pagesLiveData = MutableLiveData<MutableList<InspoPage>>(mutableListOf())
    val pagesLiveData = MutableLiveData<MutableList<InspoPage>>(mutableListOf())

    private fun pageCollection(bookName: String) =
        firestore.collection("users").document(auth.currentUser!!.uid)
            .collection("books").document(bookName)
            .collection("pages")

    fun syncPages(bookName: String) {
        pageCollection(bookName).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("RepoTest", "Error syncing pages: ${e.message}")
                return@addSnapshotListener
            }
            val fetchedList = snapshot?.toObjects(InspoPage::class.java)?.toMutableList() ?: mutableListOf()

            Log.d("RepoTest", "syncPages: fetched ${fetchedList.size} pages for '$bookName'")

            _pagesLiveData.value = fetchedList
        }
    }

    fun addPageToFirebase(bookName: String, page: InspoPage) {
        Log.d("RepoTest", "addPageToFirebase() caled for page ${page.pageID}")
        val storageRef = storage.reference
            .child("users/${auth.currentUser!!.uid}/books/$bookName/${page.pageID}.png")

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
                    pageCollection(bookName).document(page.pageID).set(pageData)
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

    fun updatePageInFirebase(bookName: String, page: InspoPage) {
        addPageToFirebase(bookName, page)
    }

    fun deletePageFromFirebase(bookName: String, pageID: String) {
        pageCollection(bookName).document(pageID).delete()
        storage.reference.child("users/${auth.currentUser!!.uid}/books/$bookName/$pageID.png").delete()
            .addOnSuccessListener {
                Log.d("RepoTest", "Page '${pageID}' deleted successfully")
            }
            .addOnFailureListener {
                Log.e("RepoTest", "Failed to delete page: ${it.message}")
            }
    }
}