import android.content.Context
import androidx.lifecycle.ViewModel
import com.codigocreativo.mobile.api.ApiService
import com.codigocreativo.mobile.api.GoogleLoginRequest
import com.codigocreativo.mobile.api.LoginRequest
import com.codigocreativo.mobile.api.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel : ViewModel() {
    private val apiService = RetrofitClient.getLogin().create(ApiService::class.java)

    suspend fun login(usuario: String, password: String): String? {
        val loginRequest = LoginRequest(usuario, password)
        val response = apiService.login(loginRequest)

        return if (response.isSuccessful) {
            val loginResponse = response.body()
            loginResponse?.token  // Devuelve el token JWT si es exitoso
        } else {
            null  // Devuelve null si la autenticación falla
        }
    }

    /*suspend fun googleLogin(googleIdToken: String): String? {
        return try {
            // Haz una llamada a tu API REST para autenticar con Google
            val response = apiService.googleLogin(GoogleLoginRequest(googleIdToken))
            if (response.isSuccessful) {
                response.body()?.jwt // Devuelve el token JWT
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }*/


    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("jwt_token", token).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }


}
