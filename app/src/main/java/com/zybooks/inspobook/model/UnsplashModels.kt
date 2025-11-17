package com.zybooks.inspobook.model

data class UnsplashUrls(
    val small: String,
    val regular: String,
    val full: String
)

data class UnsplashPhoto(
    val id: String,
    val description: String?,
    val alt_description: String?,
    val urls: UnsplashUrls
)

data class UnsplashSearchResponse(
    val total: Int,
    val total_pages: Int,
    val results: List<UnsplashPhoto>
)
