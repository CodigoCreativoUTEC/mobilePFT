package com.codigocreativo.mobile.features.equipos

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ImgBBService {
    @POST("1/upload")
    @FormUrlEncoded
    suspend fun subirImagen(
        @Field("key") apiKey: String,
        @Field("image") base64Image: String
    ): Response<ImgBBResponse>
}
