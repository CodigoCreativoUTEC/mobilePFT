package com.codigocreativo.mobile

import LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.security.MessageDigest
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    // Instancia de LoginViewModel
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Referencias a los elementos de la UI
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Configurar el botón de login
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Convertir la contraseña a SHA-256 antes de enviarla
                val hashedPassword = sha256ToHex(password)
                performLogin(username, hashedPassword)
            } else {
                Snackbar.make(it, "Debe completar ambos campos", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        // Usar coroutines para la llamada de login
        lifecycleScope.launch {
            val token = loginViewModel.login(username, password)

            if (token != null) {
                // Guardar el token JWT en SharedPreferences
                loginViewModel.saveToken(this@MainActivity, token)
                Log.d("MainActivity", "Login exitoso, token: $token")
                Snackbar.make(findViewById(R.id.main), "Login exitoso", Snackbar.LENGTH_LONG).show()


                // Aquí puedes redirigir al usuario a otra pantalla


            } else {
                Log.e("MainActivity", "Error en el login")
                Snackbar.make(findViewById(R.id.main), "Error en las credenciales", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // Función para convertir la contraseña en un hash SHA-256 y luego a hexadecimal
    private fun sha256ToHex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }  // Convierte a hexadecimal
    }
}
