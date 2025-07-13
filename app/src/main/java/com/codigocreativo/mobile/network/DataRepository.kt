package com.codigocreativo.mobile.network

import android.content.Context
import android.util.Log
import com.codigocreativo.mobile.utils.NetworkUtils
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.lang.Exception

class DataRepository {

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> {
                Log.e("DataRepository", "Timeout de conexión: ${exception.message}")
                "Tiempo de espera agotado. Verifica tu conexión a internet e intenta nuevamente."
            }
            is UnknownHostException -> {
                Log.e("DataRepository", "No se puede resolver el host: ${exception.message}")
                "No se puede conectar al servidor. Verifica tu conexión a internet."
            }
            is IOException -> {
                Log.e("DataRepository", "Error de conexión: ${exception.message}")
                "Error de conexión. Verifica tu conexión a internet e intenta nuevamente."
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string()
                Log.e("DataRepository", "Error HTTP ${exception.code()}: $errorBody")
                
                // Intentar extraer el mensaje específico del servidor
                val serverMessage = try {
                    if (errorBody != null && errorBody.contains("\"message\"")) {
                        val messageStart = errorBody.indexOf("\"message\":\"") + 11
                        val messageEnd = errorBody.indexOf("\"", messageStart)
                        if (messageStart > 10 && messageEnd > messageStart) {
                            errorBody.substring(messageStart, messageEnd)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
                
                when (exception.code()) {
                    400 -> serverMessage ?: "Datos inválidos. Verifique la información ingresada."
                    401 -> "Sesión expirada. Por favor, inicia sesión nuevamente."
                    403 -> "No tienes permisos para realizar esta acción."
                    404 -> "Recurso no encontrado en el servidor."
                    409 -> serverMessage ?: "El recurso ya existe."
                    422 -> serverMessage ?: "Datos incompletos o inválidos."
                    500 -> serverMessage ?: "Error interno del servidor. Intenta más tarde."
                    502, 503, 504 -> "Servidor temporalmente no disponible. Intenta más tarde."
                    else -> serverMessage ?: "Error del servidor: ${exception.code()}. Intenta más tarde."
                }
            }
            else -> {
                Log.e("DataRepository", "Error inesperado: ${exception.message}", exception)
                "Error inesperado. Intenta nuevamente."
            }
        }
    }

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
                Result.failure(Exception(getErrorMessage(HttpException(response))))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(getErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
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
                Result.failure(Exception(getErrorMessage(HttpException(response))))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(getErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    suspend fun <T> guardarDatos(
        token: String, 
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception(getErrorMessage(HttpException(response))))
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    suspend fun <T> eliminarDato(
        token: String, 
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DataRepository", "Error ${response.code()}: $errorBody")
                Result.failure(Exception(getErrorMessage(HttpException(response))))
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Exception: ${e.message}", e)
            Result.failure(Exception(getErrorMessage(e)))
        }
    }
}