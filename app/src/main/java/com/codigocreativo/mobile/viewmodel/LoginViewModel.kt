import android.content.Context
import androidx.lifecycle.ViewModel
import com.codigocreativo.mobile.api.ApiService
import com.codigocreativo.mobile.api.LoginRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel : ViewModel() {
    private val apiService = Retrofit.Builder()
        .baseUrl("http://gns3serv.ddns.net:8080/ServidorApp-1.0-SNAPSHOT/api/")  // Cambia esto a la URL pública de tu backend
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

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

    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("jwt_token", token).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}
