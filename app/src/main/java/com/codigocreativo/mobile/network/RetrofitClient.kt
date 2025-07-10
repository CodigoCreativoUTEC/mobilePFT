package com.codigocreativo.mobile.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://codigocreativo.ddns.net:8080/ServidorApp-1.0-SNAPSHOT/api/"
    private const val CONNECT_TIMEOUT = 30L // 30 segundos para conectar
    private const val READ_TIMEOUT = 30L // 30 segundos para leer
    private const val WRITE_TIMEOUT = 30L // 30 segundos para escribir

    private fun createOkHttpClient(token: String? = null): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Agregar token si se proporciona
        token?.let { authToken ->
            clientBuilder.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
        }

        return clientBuilder.build()
    }

    // Función para obtener una instancia de Retrofit con el token de autenticación
    fun getClient(token: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getClientSinToken(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getLogin(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getImgBBClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

