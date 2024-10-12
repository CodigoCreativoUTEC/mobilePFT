package com.codigocreativo.mobile.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Función para obtener una instancia de Retrofit con el token de autenticación
    fun getClient(token: String): Retrofit {
        // Crear un cliente HTTP que agrega el encabezado Authorization
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token") // Agrega el token en el encabezado
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("http://gns3serv.ddns.net:8080/ServidorApp-1.0-SNAPSHOT/api/")
            .client(client) // Usa el cliente con el token
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getLogin(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://gns3serv.ddns.net:8080/ServidorApp-1.0-SNAPSHOT/api/") // Cambia por tu URL del backend
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
