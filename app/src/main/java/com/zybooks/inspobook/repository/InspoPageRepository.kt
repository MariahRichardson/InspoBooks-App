package com.zybooks.inspobook.repository

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Log.e
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

            val docs = snapshot?.documents ?: emptyList()
            val fetchedList = snapshot?.toObjects(InspoPage::class.java)?.toMutableList() ?: mutableListOf()
            //val fetchedList = mutableListOf<InspoPage>()

            //map to avoid duplicate page ids
            val pagesMap = mutableMapOf<String, InspoPage>()

            Log.d("REPO", "syncdoczie ${docs.size}")
            for (doc in docs) {
                val pageID = doc.getString("pageID") ?: continue
                val imageUrl = doc.getString("imageUrl")

                val inspoPage = InspoPage(pageID, null)

                //if page id does not already exist in the map, add to it(to prevent duplicates)
                if(!pagesMap.containsKey(pageID)){
                    pagesMap[pageID] = inspoPage
                }

                // if there’s an imageUrl, fetch Bitmap from Storage
                if (imageUrl != null) {
                    val storageRef = storage.getReferenceFromUrl(imageUrl)
                    storageRef.getBytes(5 * 1024 * 1024) // limit 5MB
                        .addOnSuccessListener { bytes ->
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            inspoPage.content = bmp

                            //update the pageid with the content from firebase
                            pagesMap[pageID] = inspoPage
                            val updated = pagesMap.values.toMutableList()
                            _pagesLiveData.postValue(updated)

                            Log.d("RepositoryTest", "syncPages: fetched ${updated.size} pages for '$bookID'")

                        }
                        .addOnFailureListener {
                            Log.e("RepositoryTest", "Failed to load image for $pageID: ${it.message}")
                        }
                }
            }
        }
    }


    fun isAnyPagesInFirebase(bookID: String, onResult: (Boolean) -> Unit){
        val storageRef = storage.reference
            .child("users/${auth.currentUser!!.uid}/books/$bookID")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val isNotEmpty = listResult.items.isNotEmpty()
                Log.d("RepoTest", "Book '$bookID' has pages: $isNotEmpty")
                onResult(isNotEmpty)
            }
            .addOnFailureListener {
                Log.e("RepoTest", "Error checking folder contents: ${it.message}")
                onResult(false)
            }
    }

    fun addPageToFirebase(bookID: String, page: InspoPage, isUpdateFirebase: Boolean) {

        //if the add is not called in the updatePageToFirebase
        if(!isUpdateFirebase || page.pageID.isEmpty()) {
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

        val addedItem = _pagesLiveData.value.toMutableList()
        addedItem.add(InspoPage(page.pageID, page.content))
        _pagesLiveData.value = addedItem

        addedItem.forEach { Log.d("Repo AddPageRepo", "${it.pageID}") }
    }

    fun updatePageInFirebase(bookID: String, ID: String, page: InspoPage) {
        //pageCollection(bookName).document(page.pageID).set(page,SetOptions.merge())
        Log.d("RepoTest", "add-updatepage ${page.pageID}")
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

        val tempRemovePageIDList = _pagesLiveData.value.toMutableList()
        tempRemovePageIDList.filter{it.pageID != pageID}
        _pagesLiveData.value = tempRemovePageIDList
    }
}