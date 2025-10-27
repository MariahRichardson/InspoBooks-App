package com.zybooks.inspobook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.zybooks.inspobook.model.User
import com.zybooks.inspobook.repository.UserRepository

class UserViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    val currentUser: LiveData<FirebaseUser?> = repository.currentUser
    val user: LiveData<User?> = repository.userProfile

    fun register(email: String, password: String, username: String, pfp: String = "") =
        repository.registerUser(email, password, username, pfp)

    fun saveUserProfile(user: User) = repository.saveUserProfile(user)

    fun login(email: String, password: String) =
        repository.loginUser(email, password)

    fun logout() = repository.logout()

    fun getUserProfile(uid: String) = repository.getUserProfile(uid)

    fun updateUserProfile(uid: String, updates: Map<String, Any>) =
        repository.updateUserProfile(uid, updates)

    fun deleteUserAccount() = repository.deleteUserAccount()
}