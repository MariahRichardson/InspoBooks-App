package com.zybooks.inspobook.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UnsplashApi {

    private const val BASE_URL = "https://api.unsplash.com/"
    private const val ACCESS_KEY = "8ztm8QyC5nVBG3HjhbwlUusVB7VVZepIlC10qjlJNjk"

    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val newReq = original.newBuilder()
                .addHeader("Authorization", "Client-ID $ACCESS_KEY")
                .build()
            return chain.proceed(newReq)
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: UnsplashApiService = retrofit.create(UnsplashApiService::class.java)
}
