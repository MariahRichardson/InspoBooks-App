package com.zybooks.inspobook.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.User

class UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    // user profile LiveData (repository-level)
    val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> get() = _userProfile

    init {
        _currentUser.value = auth.currentUser
    }

    // CREATE (FirebaseAuth signup + save profile)
    fun registerUser(email: String, password: String, username: String, pfp: String = ""): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    _currentUser.value = firebaseUser

                    val user = User(
                        uid = firebaseUser?.uid ?: "",
                        email = firebaseUser?.email ?: "",
                        password = password,
                        username = username,
                        pfp = pfp
                    )

                    // Save profile to Firestore and update LiveData
                    saveUserProfile(user).observeForever { profileResult ->
                        result.value = profileResult
                    }
                } else {
                    result.value = Result.failure(task.exception ?: Exception("Registration failed"))
                }
            }
        return result
    }

    // READ (fetch profile data)
    fun getUserProfile(uid: String): LiveData<User?> {
        val userData = MutableLiveData<User?>()
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                userData.value = user
                _userProfile.value = user
            }
            .addOnFailureListener {
                userData.value = null
            }
        return userData
    }

    // UPDATE (Firestore document fields)
    fun updateUserProfile(uid: String, updates: Map<String, Any>): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                val currentUser = _userProfile.value
                val updatedUser = currentUser?.copy(
                    email = updates["email"] as? String ?: currentUser.email,
                    password = updates["password"] as? String ?: currentUser.password,
                    username = updates["username"] as? String ?: currentUser.username,
                    pfp = updates["pfp"] as? String ?: currentUser.pfp,
                    about = updates["about"] as? String ?: currentUser.about
                )
                _userProfile.value = updatedUser
                result.value = Result.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Result.failure(e)
            }

        return result
    }

    //UPDATE USERNAME AND ABOUT OF USER
    fun updateUserAboutAndUsername(updates: Map<String, Any>): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()

        firestore.collection("users").document(auth.currentUser!!.uid)
            .update(updates)
            .addOnSuccessListener {
                //after successful update, get the new data
                val userData = MutableLiveData<User?>()
                firestore.collection("users").document(auth.currentUser!!.uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        userData.value = user
                        _userProfile.value = user
                    }
                    .addOnFailureListener {
                        userData.value = null
                    }
                result.value = Result.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Result.failure(e)
            }

        return result
    }

    // DELETE

    fun deleteUserAccount(): Boolean {
        Log.d("REPOSITORYTEST", "DELETE USER ACCOUNT")
        var result = false
        val user = auth.currentUser

        val storageRef = FirebaseStorage.getInstance().reference
        val inspoBookRepo = InspoBookRepository()

        if (user != null) {
            Log.d("REPOSITORYTEST", "USER ACCOUNT NOT NULL")
            //get all bookid folders in books of the user
            //storageRef.child("users/${auth.currentUser!!.uid}/books").listAll()
                firestore.collection("users").document("${user!!.uid}").collection("books").get()
                .addOnSuccessListener { listResult ->

                    //get number of bookid folders in cloud storage
                    val totalSize = listResult.size()
                    if(totalSize == 0){
                        //if no books found in cloud storage
                        Log.d("RepositoryTest", "No books found in user ${auth.currentUser!!.uid}")
                        //delete user
                        result = deleteUserDoc()
                    }
                    else{
                        var deletedBooks = 0
                        //if there are books, get all bookID's folders and delete all data within it
                        for(bookIDSnapshot in listResult) {
                            //get name of the snapshot, which is the bookid
                            val bookID = bookIDSnapshot.id
                            //parse the as prefixed gives the full path to the book_id, get book id by getting string after the last /
                            //val bookID = bookIDPath.toString().substringAfterLast("/")
                            inspoBookRepo.deleteBooksFromFirebaseUsingID(bookID.toString(),
                                onSuccessListener = {
                                    Log.d("RepositoryTest", "User ${auth.currentUser!!.uid}, book id ${bookID} removed")
                                    deletedBooks++
                                    if(deletedBooks == totalSize){
                                        //delete user in database and in auth once all other data has been deleted
                                        result = deleteUserDoc()
                                    }
                                },
                                onFailureListener = {})
                            Log.d("RepositoryTest", "${bookID} found to delete from user")
                        }
                    }
                    Log.d("REPOSITORYTEST", "LIST ALL BOOKS")

                }
                .addOnFailureListener {
                    //result.value = Result.failure(Exception("No user logged in"))
                }
        } else {
            //result.value = Result.failure(Exception("No user logged in"))
        }
        return result
    }

    fun deleteUserDoc(): Boolean{
        val user = auth.currentUser
        var result: Boolean = false

        //delete the to-delete user id in the firestore database
        firestore.collection("users").document(auth.currentUser!!.uid).delete()
            .addOnSuccessListener {
                user!!.delete()
                    .addOnSuccessListener {
                        _currentUser.value = null
                        _userProfile.value = null
//                        result.value = Result.success(true)
                        result = true
                        Log.d("RepositoryTest", "User ${user!!.uid} has been deleted")
                    }
                    .addOnFailureListener { e ->
                        result = false
                        //result.value = Result.failure(e)
                    }
            }
            .addOnFailureListener {
                result = false
                //result.value = Result.failure(Exception("No user logged in"))
            }
        Log.d("RepositoryTest", "User ${user!!.uid} has been deleted ${result}")
        return result
    }

    // LOGIN
    fun loginUser(email: String, password: String): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser

                    // auto-fetch profile on successful login
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        getUserProfile(uid) // populates _userProfile
                    }

                    result.value = Result.success(true)
                } else {
                    result.value = Result.failure(task.exception ?: Exception("Login failed"))
                }
            }
        return result
    }

    // SAVE (called directly by ViewModel or registerUser)
    fun saveUserProfile(user: User): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        val uid = user.uid

        if (uid.isBlank()) {
            result.value = Result.failure(Exception("User UID is empty"))
            return result
        }

        val data = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "password" to user.password,
            "username" to user.username,
            "pfp" to user.pfp,
            "about" to user.about
        )

        firestore.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener {
                _userProfile.value = user
                result.value = Result.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Result.failure(e)
            }

        return result
    }

    // LOGOUT
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }
}