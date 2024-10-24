package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.ApiService
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.LoginRequest
import com.codigocreativo.mobile.network.TokenRequest
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import java.security.MessageDigest

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

        val registerButton: Button = findViewById(R.id.register)
        registerButton.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Inicializa el botón para el login común
        val traditionalLoginButton: Button = findViewById(R.id.loginButton)
        traditionalLoginButton.setOnClickListener {
            val emailEditText: EditText = findViewById(R.id.usernameEditText)
            val passwordEditText: EditText = findViewById(R.id.passwordEditText)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {

                Log.d("MainActivity", "Email: $email, Password: $password")
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
                sendGoogleIdTokenToBackend(idToken) //Envio el token al backend
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
                        SessionManager.saveSessionData(this@MainActivity, jwt.token, jwt.user)
                        Log.d("MainActivity", "JWT recibido: ${jwt.token}")
                        //Login exitoso, guardo datos y envio al usuario al dashborad
                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
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
        Log.d("MainActivity", "Enviando solicitud de login al backend")
        Log.d("MainActivity", "Email: $email, Password: $password")

        val apiService = RetrofitClient.getLogin().create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val jwtResponse = response.body()
                    if (jwtResponse != null) {
                        // Guardar el JWT y el objeto Usuario
                        SessionManager.saveSessionData(this@MainActivity, jwtResponse.token, jwtResponse.user)
                        //Login exitoso, guardo datos y envio al usuario al dashborad
                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                        Log.d("MainActivity", "JWT y Usuario guardados en sesión.")
                    } else {
                        Log.e("MainActivity", "No se recibió JWT en la respuesta.")
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error: Cuenta inactiva o Eliminada, aguarde.", Toast.LENGTH_SHORT).show()

                    Log.e("MainActivity", "Error en la respuesta del backend: ${response.code()} - ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al enviar la solicitud de login: ${e.message}", e)
            }
        }
    }

}
