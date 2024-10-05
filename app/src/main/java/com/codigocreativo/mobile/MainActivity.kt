package com.codigocreativo.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.api.ApiService
import com.codigocreativo.mobile.api.RetrofitClient
import com.codigocreativo.mobile.api.LoginRequest
import com.codigocreativo.mobile.api.TokenRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa el botón de Google Sign-In
        val googleSignInButton: SignInButton = findViewById(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener {
            startGoogleSignIn()
        }

        // Inicializa el botón para el login común
        val traditionalLoginButton: Button = findViewById(R.id.loginButton)
        traditionalLoginButton.setOnClickListener {
            val emailEditText: EditText = findViewById(R.id.usernameEditText)
            val passwordEditText: EditText = findViewById(R.id.passwordEditText)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                sendLoginRequestToBackend(email, password)
            } else {
                Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN) // Inicia la actividad para el inicio de sesión.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                Toast.makeText(this, "Inicio de sesión exitoso. Token: $idToken", Toast.LENGTH_LONG).show()
                Log.d("MainActivity", "Inicio de sesión exitoso. Token: $idToken")

                // Aquí puedes enviar el ID token a tu backend si es necesario.
                sendGoogleIdTokenToBackend(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Error en el inicio de sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "Error en el inicio de sesión: ${e.message}", e)
        }
    }

    private fun sendGoogleIdTokenToBackend(idToken: String) {
        val apiService = RetrofitClient.getLogin().create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.googleLogin(TokenRequest(idToken))
                if (response.isSuccessful) {
                    val jwt = response.body()
                    if (jwt != null) {
                        Log.d("MainActivity", "JWT recibido: ${jwt.token}")
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta del backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al enviar el token: ${e.message}", e)
            }
        }
    }

    private fun sendLoginRequestToBackend(email: String, password: String) {
        val apiService = RetrofitClient.getLogin().create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val jwt = response.body()
                    if (jwt != null) {
                        Log.d("MainActivity", "JWT recibido: ${jwt.token}")
                        // Aquí puedes almacenar el JWT o procesarlo según tus necesidades.
                    } else {
                        Log.e("MainActivity", "No se recibió JWT en la respuesta.")
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta del backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al enviar la solicitud de login: ${e.message}", e)
            }
        }
    }
}
