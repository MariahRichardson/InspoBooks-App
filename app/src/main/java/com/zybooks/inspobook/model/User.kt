package com.zybooks.inspobook.model

data class User(
    val uid: String = "",
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val pfp: String = "",
    val about: String = ""
)