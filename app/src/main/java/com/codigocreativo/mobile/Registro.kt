package com.codigocreativo.mobile

import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codigocreativo.mobile.api.ApiService
import com.codigocreativo.mobile.api.Institucion
import com.codigocreativo.mobile.api.Perfil
import com.codigocreativo.mobile.api.RetrofitClient
import com.codigocreativo.mobile.api.User
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.annotations.SerializedName


class Registro : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        fun registrarUsuario(usuario: User) {
            val apiService = RetrofitClient.getClient(token = "").create(ApiService::class.java)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.registrarUsuario(usuario)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Registro exitoso
                            val user = response.body()
                            Log.d("Registro", "Usuario registrado correctamente: $user")
                        } else {
                            // Error en el registro
                            Log.e("Error", "Error en el registro: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Excepción en el registro: ${e.message}")
                }
            }
        }



        val mainView = findViewById<RelativeLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val etCedula = findViewById<EditText>(R.id.etCedula)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etfechaNacimiento = findViewById<DatePicker>(R.id.etBirthdate)


        btnRegister.setOnClickListener {
            val cedula = etCedula.text.toString()
            val email = etEmail.text.toString()
            val contrasenia = etPassword.text.toString()
            val nombre = etFirstName.text.toString()
            val apellido = etLastName.text.toString()
            val nombreUsuario = etUsername.text.toString()
            val fechaNacimiento = etfechaNacimiento.dayOfMonth // Puedes obtener la fecha desde un DatePicker
            val perfil = Perfil(id = 1, nombrePerfil = "Administrador", estado = "ACTIVO") // Ejemplo, puedes usar un Spinner para obtener el perfil

            // Crear objeto Usuario
            val nuevoUsuario = User(
                id = 0, // Se genera automáticamente en el backend
                cedula = cedula,
                email = email,
                contrasenia = contrasenia, // Encripta antes si es necesario
                fechaNacimiento = fechaNacimiento,
                estado = "SIN_VALIDAR", // Estado por defecto
                nombre = nombre,
                apellido = apellido,
                nombreUsuario = nombreUsuario,
                idInstitucion = Institucion(id = 1, nombre = "CodigoCreativo"), // Institución por defecto
                idPerfil = perfil,
                usuariosTelefonos = emptyList() // No manejamos teléfonos en el formulario
            )

            // Registrar usuario

            registrarUsuario(nuevoUsuario)
            //Actualmente dando error al registrar TODO: Reparar tablas faltantes en la base de datos
            /*E  Error en el registro: JDBC exception executing SQL [select u1_0.ID_USUARIO,u1_0.APELLIDO,u1_0.CEDULA,u1_0.CONTRASENIA,u1_0.EMAIL,u1_0.ESTADO,u1_0.FECHA_NACIMIENTO,ii1_0.ID_INSTITUCION,ii1_0.NOMBRE,ip1_0.ID_PERFIL,ip1_0.ESTADO,ip1_0.NOMBRE_PERFIL,fp1_0.ID_PERFIL,fp1_0.ID_FUNCIONALIDAD,u1_0.NOMBRE,u1_0.nombre_usuario from USUARIOS u1_0 left join INSTITUCIONES ii1_0 on ii1_0.ID_INSTITUCION=u1_0.ID_INSTITUCION left join PERFILES ip1_0 on ip1_0.ID_PERFIL=u1_0.ID_PERFIL left join FUNCIONALIDADES_PERFILES fp1_0 on ip1_0.ID_PERFIL=fp1_0.ID_PERFIL where u1_0.ID_USUARIO=?] [ORA-00942: table or view "PFT"."FUNCIONALIDADES_PERFILES" does not exist

            https://docs.oracle.com/error-help/db/ora-00942/] [n/a]*/
        }


    }
}
