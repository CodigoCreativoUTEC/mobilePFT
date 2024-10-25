package com.codigocreativo.mobile.network

import com.codigocreativo.mobile.features.marca.Marca
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
            val response = apiCall() // Aquí no necesitas usar 'invoke', solo llama directamente la función
            if (response.isSuccessful) {  // Asegúrate de que el tipo Response<T> sea de Retrofit o similar
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

    // Función para guardar datos
    suspend fun guardarDatos(
        token: String,
        apiCall: () -> Response<Marca>? // Cambié el retorno a Response<Unit> como se indicó
    ): Result<Unit> {
        return try {
            val response = apiCall() // Aquí no necesitas usar 'invoke', solo llama directamente la función
            if (response?.isSuccessful == true) {  // Asegúrate de que el tipo Response<T> sea de Retrofit o similar
                Result.success(Unit) // Si es exitoso, devolvemos un Result exitoso
            } else {
                Result.failure(Exception("Error en la respuesta del servidor: ${response?.code()} ${response?.message()}"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Excepción HTTP: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }
}
