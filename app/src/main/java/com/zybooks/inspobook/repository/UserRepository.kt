package com.zybooks.inspobook.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.zybooks.inspobook.model.User

class UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    // user profile LiveData (repository-level)
    private val _userProfile = MutableLiveData<User?>()
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
                    pfp = updates["pfp"] as? String ?: currentUser.pfp
                )
                _userProfile.value = updatedUser
                result.value = Result.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Result.failure(e)
            }

        return result
    }

    // DELETE
    fun deleteUserAccount(): LiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        val user = auth.currentUser

        if (user != null) {
            firestore.collection("users").document(user.uid).delete()
            user.delete()
                .addOnSuccessListener {
                    _currentUser.value = null
                    _userProfile.value = null
                    result.value = Result.success(true)
                }
                .addOnFailureListener { e ->
                    result.value = Result.failure(e)
                }
        } else {
            result.value = Result.failure(Exception("No user logged in"))
        }

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
            "pfp" to user.pfp
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