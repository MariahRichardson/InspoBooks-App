package com.zybooks.inspobook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.zybooks.inspobook.model.User
import com.zybooks.inspobook.repository.UserRepository

class UserViewModel() : ViewModel() {

    private val repository: UserRepository = UserRepository()
    val currentUser: LiveData<FirebaseUser?> = repository.currentUser
    private val _user: MutableLiveData<User?> = repository._userProfile
    val user: LiveData<User?> get() = _user

    fun register(email: String, password: String, username: String, pfp: String = "") =
        repository.registerUser(email, password, username, pfp)

    fun saveUserProfile(user: User) = repository.saveUserProfile(user)

    fun login(email: String, password: String) =
        repository.loginUser(email, password)

    fun logout() = repository.logout()

    fun getUserProfile(uid: String) = repository.getUserProfile(uid)

    fun updateUserProfile(uid: String, updates: Map<String, Any>) =
        repository.updateUserProfile(uid, updates)

    fun updateUserAboutAndUsername(updates: Map<String, Any>){
        repository.updateUserAboutAndUsername(updates)
    }

    fun deleteUserAccount(): Boolean{
        repository.deleteUserAccount()
        return true
    }
}