package com.codigocreativo.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Manejar la navegación hacia la actividad de Registro cuando se haga clic en el TextView
        val registerTextView = findViewById<TextView>(R.id.registerHereTextView)
        registerTextView?.setOnClickListener {
            // Crear el Intent para iniciar la actividad Registro
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Encontrar el botón de iniciar sesión
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)

        // Al hacer clic en el botón, inicia DashboardActivity
        loginButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
