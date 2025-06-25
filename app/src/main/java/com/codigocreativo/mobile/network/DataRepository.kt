package com.codigocreativo.mobile.network

import android.util.Log
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception

class DataRepository {

    // Función suspend genérica para obtener datos de cualquier tipo T
    suspend fun <T> obtenerDatos(
        token: String,
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("No se encontraron datos"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception("Error en la respuesta del servidor: ${response.code()} ${response.message()}. Detalles: $errorBody"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("DataRepository", "HttpException: ${e.message()}, Error body: $errorBody")
            Result.failure(Exception("Excepción HTTP: ${e.message()}. Detalles: $errorBody"))
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun <T> obtenerDatosSinToken(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall.invoke()
            Log.d("DataRepository", "Response code: ${response.code()}")
            Log.d("DataRepository", "Response message: ${response.message()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("No se encontraron datos"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception("Error en la respuesta del servidor: ${response.code()} ${response.message()}. Detalles: $errorBody"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("DataRepository", "HttpException: ${e.message()}, Error body: $errorBody")
            Result.failure(Exception("Excepción HTTP: ${e.message()}. Detalles: $errorBody"))
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun <T> guardarDatos(token: String, apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception("Error: ${response.code()} ${response.message()}. Detalles: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun <T> eliminarDato(token: String, apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception("Error: ${response.code()} ${response.message()}. Detalles: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }


}