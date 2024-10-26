package com.codigocreativo.mobile.network

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
                Result.failure(Exception("Error en la respuesta del servidor: ${response.code()} ${response.message()}"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Excepción HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun <T> guardarDatos(token: String, apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun <T> eliminarDato(token: String, apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}