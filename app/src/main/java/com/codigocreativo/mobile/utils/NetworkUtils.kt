package com.codigocreativo.mobile.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object NetworkUtils {

    /**
     * Verifica si hay conexión a internet disponible
     */
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error verificando conectividad: ${e.message}")
            false
        }
    }

    /**
     * Obtiene el tipo de conexión actual
     */
    fun getConnectionType(context: Context?): String {
        if (context == null) return "Desconocido"
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return "Sin conexión"
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return "Sin conexión"
            
            when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Datos móviles"
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Otro"
            }
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error obteniendo tipo de conexión: ${e.message}")
            "Desconocido"
        }
    }

    /**
     * Verifica si la conexión es estable
     */
    fun isConnectionStable(context: Context?): Boolean {
        if (context == null) return false
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error verificando estabilidad de conexión: ${e.message}")
            false
        }
    }

    /**
     * Obtiene información detallada de la conexión para debugging
     */
    fun getConnectionInfo(context: Context?): String {
        if (context == null) return "Contexto no disponible"
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val activeNetwork = network?.let { connectivityManager.getNetworkCapabilities(it) }
            
            val isConnected = activeNetwork?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val isValidated = activeNetwork?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            val connectionType = getConnectionType(context)
            
            "Conectado: $isConnected, Validado: $isValidated, Tipo: $connectionType"
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error obteniendo información de conexión: ${e.message}")
            "Error obteniendo información de conexión"
        }
    }
} 