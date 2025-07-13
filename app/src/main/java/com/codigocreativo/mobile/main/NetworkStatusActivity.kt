package com.codigocreativo.mobile.main

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NetworkStatusActivity : AppCompatActivity() {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_status)

        Log.d("NetworkStatusActivity", "Verificando estado de red")

        updateNetworkStatus()
        testServerConnection()

        // Botón para actualizar el estado
        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            updateNetworkStatus()
            testServerConnection()
        }

        // Botón para volver
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun updateNetworkStatus() {
        val statusTextView = findViewById<TextView>(R.id.tv_network_status)
        val connectionInfoTextView = findViewById<TextView>(R.id.tv_connection_info)

        val isNetworkAvailable = NetworkUtils.isNetworkAvailable(this)
        val connectionType = NetworkUtils.getConnectionType(this)
        val connectionInfo = NetworkUtils.getConnectionInfo(this)

        statusTextView.text = if (isNetworkAvailable) {
            "✅ Conexión a internet disponible"
        } else {
            "❌ No hay conexión a internet"
        }

        connectionInfoTextView.text = """
            Tipo de conexión: $connectionType
            Información detallada: $connectionInfo
        """.trimIndent()

        Log.d("NetworkStatusActivity", "Estado de red actualizado: $connectionInfo")
    }

    private fun testServerConnection() {
        val serverStatusTextView = findViewById<TextView>(R.id.tv_server_status)
        val serverInfoTextView = findViewById<TextView>(R.id.tv_server_info)

        serverStatusTextView.text = "🔄 Probando conexión al servidor..."

        CoroutineScope(ioDispatcher).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("http://codigocreativo.ddns.net:8080/ServidorApp-1.0-SNAPSHOT/api/")
                    .build()

                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseMessage = response.message

                withContext(mainDispatcher) {
                    serverStatusTextView.text = if (response.isSuccessful) {
                        "✅ Servidor accesible"
                    } else {
                        "⚠️ Servidor respondió con error"
                    }

                    serverInfoTextView.text = """
                        Código de respuesta: $responseCode
                        Mensaje: $responseMessage
                        URL: codigocreativo.ddns.net:8080
                    """.trimIndent()

                    Log.d("NetworkStatusActivity", "Prueba de servidor completada: $responseCode $responseMessage")
                }
            } catch (e: Exception) {
                withContext(mainDispatcher) {
                    serverStatusTextView.text =  "No se puede conectar al servidor"
                    serverInfoTextView.text = """
                        Error: ${e.message}
                        URL: codigocreativo.ddns.net:8080
                        
                        Posibles causas:
                        • El servidor está caído
                        • Problemas de DNS
                        • Firewall bloqueando la conexión
                        • Problemas de red
                    """.trimIndent()

                    Log.e("NetworkStatusActivity", "Error probando servidor: ${e.message}", e)
                }
            }
        }
    }
} 