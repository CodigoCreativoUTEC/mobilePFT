package com.codigocreativo.mobile.main

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.features.usuarios.UsuariosApiService
import com.codigocreativo.mobile.features.usuarios.Usuario
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DetallePerfilUsuarioActivity : AppCompatActivity() {

    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_perfi_usuario)

        // Obtener el token de la sesión
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los detalles del usuario usando el token
            cargarDetallesUsuario(token)
        } else {
            Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun cargarDetallesUsuario(token: String) {
        val apiService = RetrofitClient.getClient(token).create(UsuariosApiService::class.java)

        lifecycleScope.launch {
            try {
                // Llamar al repository para obtener los datos del usuario
                val emailUserLog = SessionManager.getUser(this@DetallePerfilUsuarioActivity)?.email
                val result = dataRepository.obtenerDatos(
                    token = token,
                    apiCall = { apiService.buscarUsuarioPorEmail("Bearer $token",
                        emailUserLog.toString()
                    ) }
                )

                result.onSuccess { usuario ->
                    // Actualizar la interfaz con los datos del usuario
                    actualizarInterfazUsuario(usuario)
                }.onFailure { error ->
                    Log.e("DetallePerfilUsuarioActivity", "Error al cargar los datos del usuario: ${error.message}")
                    Snackbar.make(findViewById(R.id.main), "Error al cargar los datos del usuario: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetallePerfilUsuarioActivity", "Error inesperado: ${e.message}", e)
                Snackbar.make(findViewById(R.id.main), "Error inesperado al cargar los datos del usuario", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun actualizarInterfazUsuario(usuario: Usuario) {
        val tvNombre: TextView = findViewById(R.id.tv_user_name)
        val tvEmail: TextView = findViewById(R.id.tv_user_email)

        // Configura los TextViews con los datos del usuario
        tvNombre.text = "${usuario.nombre} ${usuario.apellido}"
        tvEmail.text = usuario.email
        // Agrega más campos si es necesario
    }
}
