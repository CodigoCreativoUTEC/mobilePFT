package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.SessionManager

class AccessDeniedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_denied)

        Log.d("AccessDeniedActivity", "Acceso denegado - mostrando pantalla de error")

        // Configurar el mensaje de acceso denegado
        val messageTextView = findViewById<TextView>(R.id.tv_access_denied_message)
        messageTextView.text = getString(R.string.access_denied_message)

        // Configurar el botón de volver al login
        val backToLoginButton = findViewById<Button>(R.id.btn_back_to_login)
        backToLoginButton.setOnClickListener {
            Log.d("AccessDeniedActivity", "Usuario regresando al login")
            
            // Limpiar la sesión actual
            SessionManager.clearSession(this)
            
            // Navegar de vuelta a la pantalla de login
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        // Prevenir que el usuario regrese con el botón atrás
        // Solo permitir regresar al login usando el botón
        Log.d("AccessDeniedActivity", "Botón atrás presionado - ignorando")
        // No llamamos a super.onBackPressed() para bloquear el back
    }
} 