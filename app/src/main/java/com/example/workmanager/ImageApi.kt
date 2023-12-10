package com.example.workmanager

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface ImageApi {

    @GET("/u/104721363?s=400&u=c84871d1de6e167520d1a92f559ea24234d787a6&v=4")
    suspend fun downloadImage(): Response<ResponseBody>


    companion object {
        val api by lazy {
            Retrofit.Builder()
                .baseUrl("https://avatars.githubusercontent.com")
                .build()
                .create(ImageApi::class.java)
        }
    }


}









