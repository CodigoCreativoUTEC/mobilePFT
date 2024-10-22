package com.codigocreativo.mobile.main

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.ApiService
import com.codigocreativo.mobile.network.Institucion
import com.codigocreativo.mobile.network.Perfil
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.User
import com.codigocreativo.mobile.network.UsuariosTelefono
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class Registro : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Función para registrar un usuario en el backend
        fun registrarUsuario(usuario: User) {
            // Inicializar el servicio de la API
            val apiService = RetrofitClient.getClient(token = "").create(ApiService::class.java)

            // Ejecutar la solicitud de registro en un hilo de fondo
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.registrarUsuario(usuario)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Registro exitoso
                            Log.d("Registro", "Usuario registrado correctamente: ${response.body()}")
                            Toast.makeText(this@Registro, "Registro exitoso", Toast.LENGTH_LONG).show()
                            // Redirigir al login
                            val intent = Intent(this@Registro, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Error en el registro
                            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                            Log.e("Error", "Error en el registro: $errorMsg")
                            Toast.makeText(this@Registro, "Error en el registro: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string() ?: "Error desconocido"
                    withContext(Dispatchers.Main) {
                        Log.e("Error", "Error en la solicitud: $errorBody")
                        Toast.makeText(this@Registro, "Error en la solicitud: $errorBody", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Manejo de excepciones en caso de fallo en la solicitud
                    withContext(Dispatchers.Main) {
                        Log.e("Error", "Excepción en el registro: ${e.message}")
                        Toast.makeText(this@Registro, "Excepción en el registro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Obtener la referencia al layout principal y aplicar los márgenes de las barras del sistema
        val mainView = findViewById<RelativeLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencias a los elementos de la interfaz
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val etCedula = findViewById<EditText>(R.id.etCedula)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etBirthdate = findViewById<EditText>(R.id.etBirthdate)
        val etPhone = findViewById<EditText>(R.id.etPhone)

        // Configurar el DatePickerDialog para el campo de fecha de nacimiento
        etBirthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18) // Establecer la fecha inicial para alguien mayor de 18 años
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Crear y mostrar el DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha seleccionada y establecerla en el campo de texto
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etBirthdate.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        // Deshabilitar la edición del campo de nombre de usuario
        etUsername.isEnabled = false

        // Actualizar el nombre de usuario a medida que se ingresa el nombre y el apellido
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nombre = etFirstName.text.toString().lowercase(Locale.getDefault())
                val apellido = etLastName.text.toString().lowercase(Locale.getDefault())
                etUsername.setText("$nombre.$apellido")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etFirstName.addTextChangedListener(textWatcher)
        etLastName.addTextChangedListener(textWatcher)

        // Configurar el botón de registro para registrar un nuevo usuario
        btnRegister.setOnClickListener {
            // Obtener los valores de los campos de texto
            val cedula = etCedula.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val contrasenia = etPassword.text.toString().trim()
            val confirmarContrasenia = etConfirmPassword.text.toString().trim()
            val nombre = etFirstName.text.toString().trim()
            val apellido = etLastName.text.toString().trim()
            val nombreUsuario = etUsername.text.toString().trim()
            val fechaNacimiento = etBirthdate.text.toString().trim()
            val telefono = etPhone.text.toString().trim()

            // Validar el formato de la fecha de nacimiento
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate: Date? = try {
                dateFormat.parse(fechaNacimiento)
            } catch (e: Exception) {
                null
            }

            if (birthDate == null) {
                Log.e("Error", "Fecha de nacimiento no válida")
                Toast.makeText(this, "Fecha de nacimiento no válida", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validar que las contraseñas coincidan
            if (contrasenia != confirmarContrasenia) {
                Log.e("Error", "Las contraseñas no coinciden")
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validar que el email sea válido
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.e("Error", "Correo electrónico no válido")
                Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Crear un perfil predeterminado (en este caso, Administrador)
            val perfil = Perfil(id = 1, nombrePerfil = "Administrador", Estado.ACTIVO, funcionalidades = emptyList())

            // Crear la lista de teléfonos
            val usuariosTelefonos = if (telefono.isNotEmpty()) {
                listOf(UsuariosTelefono(id = 0, numero = telefono))
            } else {
                emptyList()
            }

            // Crear el objeto Usuario con la información proporcionada
            val nuevoUsuario = User(
                id = 0, // El ID se genera automáticamente en el backend
                cedula = cedula,
                email = email,
                contrasenia = contrasenia,
                fechaNacimiento = dateFormat.format(birthDate),
                estado = Estado.SIN_VALIDAR, // Estado por defecto
                nombre = nombre,
                apellido = apellido,
                nombreUsuario = nombreUsuario,
                idInstitucion = Institucion(id = 1, nombre = "CodigoCreativo"), // Institución predeterminada
                idPerfil = perfil,
                usuariosTelefonos = usuariosTelefonos // Agregar el teléfono si se proporciona
            )

            // Llamar a la función para registrar al usuario
            registrarUsuario(nuevoUsuario)
        }
    }
}
