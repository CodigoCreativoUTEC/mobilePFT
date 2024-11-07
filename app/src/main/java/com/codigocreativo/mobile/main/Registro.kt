package com.codigocreativo.mobile.main

import android.app.DatePickerDialog
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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.perfiles.SelectorPerfilFragment
import com.codigocreativo.mobile.features.usuarios.Usuario
import com.codigocreativo.mobile.features.usuarios.UsuariosApiService
import com.codigocreativo.mobile.features.institucion.Institucion
import com.codigocreativo.mobile.features.perfiles.Perfil
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.features.usuarios.Telefono
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Registro : AppCompatActivity() {

    private val dataRepository = DataRepository()

    private lateinit var perfilPickerFragment: SelectorPerfilFragment
    private var perfilSeleccionado: Perfil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Crear una instancia de SelectorPerfilFragment
        perfilPickerFragment = SelectorPerfilFragment()



        // Observe the loading state of the fragment
        perfilPickerFragment.isDataLoaded.observe(this, Observer { isLoaded ->
            if (isLoaded) {
                // Now you can safely access getSelectedPerfil
                perfilSeleccionado = perfilPickerFragment.getSelectedPerfil()
            }
        })

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

            if (perfilSeleccionado == null) {
                Log.e("Error", "Debe seleccionar un perfil")
                Toast.makeText(this, "Debe seleccionar un perfil", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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

            // Crear la lista de teléfonos
            val usuariosTelefonos = if (telefono.isNotEmpty()) {
                listOf(Telefono(id = 0, numero = telefono))
            } else {
                emptyList()
            }

            // Crear el objeto Usuario con la información proporcionada
            val nuevoUsuario = Usuario(
                id = 0, // El ID se genera automáticamente en el backend
                cedula = cedula,
                email = email,
                contrasenia = contrasenia,
                fechaNacimiento = dateFormat.format(birthDate),
                estado = Estado.SIN_VALIDAR, // Estado por defecto
                nombre = nombre,
                apellido = apellido,
                nombreUsuario = nombreUsuario,
                idInstitucion = Institucion(
                    id = 1,
                    nombre = "CodigoCreativo"
                ), // Institución predeterminada
                idPerfil = perfilSeleccionado!!,
                usuariosTelefonos = usuariosTelefonos // Agregar el teléfono si se proporciona
            )

            // Llamar a la función para registrar al usuario
            registrarUsuario(nuevoUsuario)
        }
    }

    // Función para registrar un usuario en el backend
    private fun registrarUsuario(usuario: Usuario) {
        val token = SessionManager.getToken(this)
        if (token != null) {
            val apiService = RetrofitClient.getClient(token = token).create(UsuariosApiService::class.java)
            val dataRepository = DataRepository()

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(
                    token = token,
                    apiCall = { apiService.crearUsuario("Bearer $token", usuario) }
                )

                result.onSuccess { response ->
                    Log.i("Response", response.toString())
                }.onFailure { error ->
                    Log.e("Error", error.message.toString())
                }
            }
        }
    }
}