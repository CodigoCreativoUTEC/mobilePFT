package com.codigocreativo.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.api.ApiService
import com.codigocreativo.mobile.api.RetrofitClient
import com.codigocreativo.mobile.api.TokenRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        credentialManager = CredentialManager.create(this)

        // Iniciar el flujo de autenticación al cargar la actividad
        requestSignIn()
    }

    private fun requestSignIn() {
        // Configura la solicitud de credenciales de Google
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .setAutoSelectEnabled(true)
                    .build()
            )
            .build()


        lifecycleScope.launch {
            try {
                // Llamar al Credential Manager para obtener las credenciales
                val credentialResponse = credentialManager.getCredential(this@MainActivity, request)
                handleCredential(credentialResponse)
            } catch (e: NoCredentialException) {
                // No se encontraron credenciales
                Toast.makeText(this@MainActivity, "No se encontraron credenciales, iniciar sesión manualmente.", Toast.LENGTH_SHORT).show()
                startLegacyGoogleSignIn()
            } catch (e: Exception) {
                // Manejo de otros errores
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCredential(credentialResponse: GetCredentialResponse) {
        val credential = credentialResponse.credential
        Log.d("MainActivity", "Credencial: ${credential.type}")

        if (credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL") {
            try {
                val dataBundle = credential.data as Bundle
                val idToken = dataBundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")

                if (idToken != null) {
                    Toast.makeText(this, "Inicio de sesión exitoso. Token: $idToken", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Inicio de sesión exitoso. Token: $idToken")

                    // Ahora que tienes el idToken, envíalo al backend
                    sendGoogleIdTokenToBackend(idToken)

                    decodeJWT(idToken)
                } else {
                    Log.e("MainActivity", "El ID token no está presente en la credencial.")
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error al procesar la credencial: ${e.message}", e)
            }
        } else {
            Log.e("MainActivity", "Tipo de credencial no esperado.")
        }
    }



    private fun sendGoogleIdTokenToBackend(idToken: String) {
        val apiService = RetrofitClient.getLogin().create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                // Usa la clase TokenRequest que ahora tiene la propiedad idToken
                val response = apiService.googleLogin(TokenRequest(idToken))
                if (response.isSuccessful) {
                    val jwt = response.body()
                    Log.d("MainActivity", "JWT recibido: $jwt")
                    // Puedes almacenar el JWT o procesarlo según tus necesidades
                } else {
                    Log.e("MainActivity", "Error en la respuesta del backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al enviar el token: ${e.message}", e)
            }
        }
    }



    private fun startLegacyGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id)) // Asegúrate de usar el Client ID correcto aquí también.
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                decodeJWT(idToken)
                Log.d("com.codigocreativo.mobile.MainActivity", "Inicio de sesión exitoso. Token: $idToken")
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeJWT(token: String) {
        val parts = token.split(".")
        if (parts.size == 3) {
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            Log.d("MainActivity", "Payload: $payload")
            Toast.makeText(this, "Payload: $payload", Toast.LENGTH_LONG).show()
        } else {
            Log.e("MainActivity", "JWT Token inválido.")
        }
    }
}
