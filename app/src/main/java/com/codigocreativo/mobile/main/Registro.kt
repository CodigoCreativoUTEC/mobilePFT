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
import com.google.android.material.snackbar.Snackbar
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

        // Inicializar SelectorPerfilFragment
        perfilPickerFragment = SelectorPerfilFragment()
        perfilPickerFragment.isDataLoaded.observe(this, Observer { isLoaded ->
            if (isLoaded) {
                perfilSeleccionado = perfilPickerFragment.getSelectedPerfil()
            }
        })

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

        etBirthdate.setOnClickListener { showDatePicker(etBirthdate) }
        etUsername.isEnabled = false
        configureUsernameGeneration(etFirstName, etLastName, etUsername)

        btnRegister.setOnClickListener {
          //  if (perfilSeleccionado == null) {
            //    showToast("Debe seleccionar un perfil")
            //    return@setOnClickListener
      //      }

            val birthDate = validateAndGetDate(etBirthdate.text.toString())
            if (birthDate == null) {
                showToast("Fecha de nacimiento no válida")
                return@setOnClickListener
            }

            if (!validateFields(etCedula, etEmail, etPassword, etConfirmPassword)) return@setOnClickListener

            val nuevoUsuario = createUsuario(etCedula, etEmail, etPassword, etFirstName, etLastName, etUsername, birthDate, etPhone)
            registrarUsuario(nuevoUsuario)
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val (year, month, day) = Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            editText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
        }, year, month, day).show()
    }

    private fun configureUsernameGeneration(etFirstName: EditText, etLastName: EditText, etUsername: EditText) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = "${etFirstName.text.toString().lowercase(Locale.getDefault())}.${etLastName.text.toString().lowercase(Locale.getDefault())}"
                etUsername.setText(username)
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etFirstName.addTextChangedListener(textWatcher)
        etLastName.addTextChangedListener(textWatcher)
    }

    private fun validateAndGetDate(dateText: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateText)
        } catch (e: Exception) {
            null
        }
    }

    private fun validateFields(
        etCedula: EditText, etEmail: EditText, etPassword: EditText, etConfirmPassword: EditText
    ): Boolean {
        if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
            showToast("Las contraseñas no coinciden")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
            showToast("Correo electrónico no válido")
            return false
        }
        return true
    }

    private fun createUsuario(
        etCedula: EditText, etEmail: EditText, etPassword: EditText, etFirstName: EditText, etLastName: EditText,
        etUsername: EditText, birthDate: Date, etPhone: EditText
    ): Usuario {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return Usuario(
            id = 0,
            cedula = etCedula.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            contrasenia = etPassword.text.toString().trim(),
            fechaNacimiento = dateFormat.format(birthDate),
            estado = Estado.SIN_VALIDAR,
            nombre = etFirstName.text.toString().trim(),
            apellido = etLastName.text.toString().trim(),
            nombreUsuario = etUsername.text.toString().trim(),
            idInstitucion = Institucion(id = 1, nombre = "CodigoCreativo"),
            idPerfil = perfilSeleccionado!!,
            usuariosTelefonos = listOfNotNull(etPhone.text.toString().takeIf { it.isNotEmpty() }?.let { Telefono(0, it) })
        )
    }

    private fun registrarUsuario(usuario: Usuario) {
        val token = SessionManager.getToken(this)
        if (token != null) {
            val apiService = RetrofitClient.getClient(token).create(UsuariosApiService::class.java)
            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token, { apiService.crearUsuario("Bearer $token", usuario) })
                result.onSuccess {
                    Snackbar.make(findViewById(android.R.id.content), "Usuario registrado correctamente", Snackbar.LENGTH_LONG).show()
                    finish() // Opcionalmente, finalizar actividad o redirigir
                }.onFailure { error ->
                    Log.e("Registro", "Error al registrar usuario: ${error.message}")
                    Snackbar.make(findViewById(android.R.id.content), "Error al registrar usuario: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            showToast("Token no encontrado, por favor inicia sesión")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
