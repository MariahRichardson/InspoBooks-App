package com.zybooks.inspobook.network

import com.zybooks.inspobook.model.UnsplashPhoto
import com.zybooks.inspobook.model.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("order_by") orderBy: String? = null,  // "relevant" or "latest"
        @Query("color") color: String? = null        // e.g. "red", "green", "blue"
    ): UnsplashSearchResponse

    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("count") count: Int,
        @Query("query") query: String? = null
    ): List<UnsplashPhoto>

}
