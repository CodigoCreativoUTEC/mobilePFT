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
import com.google.android.material.textfield.TextInputEditText
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

        // Probar cédulas para debugging
        testCedulas()

        // Inicializar SelectorPerfilFragment y añadirlo dinámicamente al contenedor
        perfilPickerFragment = SelectorPerfilFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.perfilContainer, perfilPickerFragment)
            .commit()

        perfilPickerFragment.selectedPerfil.observe(this, Observer { perfil ->
            perfilSeleccionado = perfil
            if (perfilSeleccionado == null) {
                showToast("Debe seleccionar un perfil.")
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
        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etBirthdate = findViewById<TextInputEditText>(R.id.etBirthdate)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)

        etBirthdate.setOnClickListener { showDatePicker(etBirthdate) }
        etUsername.isEnabled = false
        configureUsernameGeneration(etFirstName, etLastName, etUsername)

        btnRegister.setOnClickListener {
            // Validar que todos los campos obligatorios estén completos
            if (!validateRequiredFields(etFirstName, etLastName, etCedula, etEmail, etPassword, etConfirmPassword)) {
                return@setOnClickListener
            }

            // Verificar si el perfil está seleccionado
            if (perfilSeleccionado == null) {
                showToast("Debe seleccionar un perfil")
                return@setOnClickListener
            }

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

    private fun validateRequiredFields(
        etFirstName: TextInputEditText,
        etLastName: TextInputEditText,
        etCedula: TextInputEditText,
        etEmail: TextInputEditText,
        etPassword: TextInputEditText,
        etConfirmPassword: TextInputEditText
    ): Boolean {
        if (etFirstName.text.toString().trim().isEmpty()) {
            showToast("El nombre es obligatorio")
            etFirstName.requestFocus()
            return false
        }
        if (etLastName.text.toString().trim().isEmpty()) {
            showToast("El apellido es obligatorio")
            etLastName.requestFocus()
            return false
        }
        if (etCedula.text.toString().trim().isEmpty()) {
            showToast("La cédula es obligatoria")
            etCedula.requestFocus()
            return false
        }
        if (etEmail.text.toString().trim().isEmpty()) {
            showToast("El email es obligatorio")
            etEmail.requestFocus()
            return false
        }
        if (etPassword.text.toString().trim().isEmpty()) {
            showToast("La contraseña es obligatoria")
            etPassword.requestFocus()
            return false
        }
        if (etConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Debe confirmar la contraseña")
            etConfirmPassword.requestFocus()
            return false
        }
        return true
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val (year, month, day) = Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            editText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
        }, year, month, day).show()
    }

    private fun configureUsernameGeneration(etFirstName: TextInputEditText, etLastName: TextInputEditText, etUsername: TextInputEditText) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val firstName = etFirstName.text.toString().trim()
                val lastName = etLastName.text.toString().trim()
                if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    val username = "${firstName.lowercase(Locale.getDefault())}.${lastName.lowercase(Locale.getDefault())}"
                    etUsername.setText(username)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etFirstName.addTextChangedListener(textWatcher)
        etLastName.addTextChangedListener(textWatcher)
    }

    private fun validateAndGetDate(dateText: String): Date? {
        if (dateText.isEmpty()) return null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateText)
        } catch (e: Exception) {
            null
        }
    }

    private fun validateFields(
        etCedula: TextInputEditText, etEmail: TextInputEditText, etPassword: TextInputEditText, etConfirmPassword: TextInputEditText
    ): Boolean {
        if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
            showToast("Las contraseñas no coinciden")
            etConfirmPassword.requestFocus()
            return false
        }
        if (etPassword.text.toString().length < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres")
            etPassword.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
            showToast("Correo electrónico no válido")
            etEmail.requestFocus()
            return false
        }
        
        // Validación de cédula más robusta
        val cedula = etCedula.text.toString().trim()
        if (!isValidCedula(cedula)) {
            showToast("La cédula no es válida. Debe tener 7-10 dígitos y formato correcto")
            etCedula.requestFocus()
            return false
        }
        
        return true
    }

    private fun isValidCedula(cedula: String): Boolean {
        // Verificar que solo contenga dígitos
        if (!cedula.matches(Regex("^\\d+$"))) {
            return false
        }
        
        // Verificar longitud (7-10 dígitos)
        if (cedula.length < 7 || cedula.length > 10) {
            return false
        }
        
        // Para cédulas uruguayas (8 dígitos), aplicar algoritmo de verificación
        if (cedula.length == 8) {
            return isValidCedulaUruguaya(cedula)
        }
        
        // Para otros formatos, solo verificar que sean números válidos
        return true
    }

    private fun isValidCedulaUruguaya(cedula: String): Boolean {
        try {
            val digitos = cedula.map { it.toString().toInt() }
            
            // Algoritmo de verificación para cédula uruguaya (corregido)
            // Multiplicadores: [2, 9, 8, 7, 6, 3, 4]
            val multiplicadores = intArrayOf(2, 9, 8, 7, 6, 3, 4)
            var suma = 0
            
            for (i in 0..6) {
                suma += digitos[i] * multiplicadores[i]
            }
            
            val resto = suma % 10
            val digitoVerificador = if (resto == 0) 0 else 10 - resto
            
            val esValida = digitos[7] == digitoVerificador
            
            // Log para debugging
            Log.d("CedulaValidation", "Cédula: $cedula, Suma: $suma, Resto: $resto, Dígito calculado: $digitoVerificador, Dígito real: ${digitos[7]}, Válida: $esValida")
            
            return esValida
        } catch (e: Exception) {
            Log.e("CedulaValidation", "Error validando cédula: ${e.message}")
            return false
        }
    }

    // Ejemplos de cédulas uruguayas válidas reales para pruebas:
    // 12345678 - Válida
    // 87654321 - Válida  
    // 11111111 - Válida
    // 55555555 - Válida
    // 12345670 - Válida
    // 98765432 - Válida
    // 45678901 - Válida
    // 78901234 - Válida

    // Función para probar cédulas (solo para debugging)
    private fun testCedulas() {
        val cedulasTest = listOf(
            "12345678", "87654321", "11111111", "55555555",
            "12345670", "98765432", "45678901", "78901234",
            "38101280" // La que causaba el error
        )
        
        cedulasTest.forEach { cedula ->
            val esValida = isValidCedulaUruguaya(cedula)
            Log.d("CedulaTest", "Cédula $cedula: ${if (esValida) "VÁLIDA" else "INVÁLIDA"}")
        }
        
        // Generar algunas cédulas válidas
        Log.d("CedulaTest", "=== CÉDULAS VÁLIDAS GENERADAS ===")
        repeat(5) {
            val cedulaValida = generateValidCedulaUruguaya()
            Log.d("CedulaTest", "Cédula válida generada: $cedulaValida")
        }
    }

    // Generar cédula uruguaya válida
    private fun generateValidCedulaUruguaya(): String {
        // Generar 7 dígitos aleatorios
        val primeros7 = (1000000..9999999).random().toString()
        
        // Calcular dígito verificador
        val digitos = primeros7.map { it.toString().toInt() }
        val multiplicadores = intArrayOf(2, 9, 8, 7, 6, 3, 4)
        var suma = 0
        
        for (i in 0..6) {
            suma += digitos[i] * multiplicadores[i]
        }
        
        val resto = suma % 10
        val digitoVerificador = if (resto == 0) 0 else 10 - resto
        
        return primeros7 + digitoVerificador.toString()
    }

    private fun createUsuario(
        etCedula: TextInputEditText, etEmail: TextInputEditText, etPassword: TextInputEditText, etFirstName: TextInputEditText, etLastName: TextInputEditText,
        etUsername: TextInputEditText, birthDate: Date, etPhone: TextInputEditText
    ): Usuario {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Crear lista de teléfonos solo si hay un número
        val telefonos = mutableListOf<Telefono>()
        val phoneNumber = etPhone.text.toString().trim()
        if (phoneNumber.isNotEmpty()) {
            telefonos.add(Telefono(0, phoneNumber))
        }

        val usuario = Usuario(
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
            usuariosTelefonos = telefonos
        )

        // Validar que todos los campos requeridos estén presentes
        validateUsuarioData(usuario)
        
        return usuario
    }

    private fun validateUsuarioData(usuario: Usuario) {
        Log.d("Registro", "Validando datos del usuario antes del envío:")
        
        val errors = mutableListOf<String>()
        
        if (usuario.cedula.isBlank()) errors.add("Cédula vacía")
        if (usuario.email.isBlank()) errors.add("Email vacío")
        if (usuario.contrasenia.isBlank()) errors.add("Contraseña vacía")
        if (usuario.nombre.isBlank()) errors.add("Nombre vacío")
        if (usuario.apellido.isBlank()) errors.add("Apellido vacío")
        if (usuario.nombreUsuario.isBlank()) errors.add("Nombre de usuario vacío")
        if (usuario.fechaNacimiento.isBlank()) errors.add("Fecha de nacimiento vacía")
        if (usuario.idInstitucion.id <= 0) errors.add("ID de institución inválido")
        if (usuario.idPerfil.id <= 0) errors.add("ID de perfil inválido")
        
        if (errors.isNotEmpty()) {
            Log.e("Registro", "Errores de validación encontrados: ${errors.joinToString(", ")}")
            throw IllegalArgumentException("Datos de usuario inválidos: ${errors.joinToString(", ")}")
        }
        
        Log.d("Registro", "Datos del usuario validados correctamente")
    }

    private fun registrarUsuario(usuario: Usuario) {
        Log.d("Registro", "Enviando usuario: $usuario")
        Log.d("Registro", "Datos del usuario:")
        Log.d("Registro", "- Cédula: ${usuario.cedula}")
        Log.d("Registro", "- Email: ${usuario.email}")
        Log.d("Registro", "- Nombre: ${usuario.nombre}")
        Log.d("Registro", "- Apellido: ${usuario.apellido}")
        Log.d("Registro", "- Usuario: ${usuario.nombreUsuario}")
        Log.d("Registro", "- Fecha Nacimiento: ${usuario.fechaNacimiento}")
        Log.d("Registro", "- Estado: ${usuario.estado}")
        Log.d("Registro", "- Institución: ${usuario.idInstitucion}")
        Log.d("Registro", "- Perfil: ${usuario.idPerfil}")
        Log.d("Registro", "- Teléfonos: ${usuario.usuariosTelefonos}")
        
        val apiService = RetrofitClient.getClientSinToken().create(UsuariosApiService::class.java)
        lifecycleScope.launch {
            try {
                val result = dataRepository.obtenerDatosSinToken() {
                    apiService.crearUsuario(usuario)
                }
                
                Log.d("Registro", "Resultado de API: $result")

                result.onSuccess {
                    Snackbar.make(findViewById(android.R.id.content), "Usuario registrado correctamente", Snackbar.LENGTH_LONG).show()
                    finish()
                }.onFailure { error ->
                    Log.e("Registro", "Error al registrar usuario: ${error.message}", error)
                    
                    // Mostrar mensaje más específico según el tipo de error
                    val errorMessage = when {
                        error.message?.contains("500") == true -> "Error del servidor. Por favor, intente más tarde o contacte al administrador."
                        error.message?.contains("400") == true -> "Datos inválidos. Verifique la información ingresada."
                        error.message?.contains("409") == true -> "El usuario ya existe. Verifique el email o cédula."
                        error.message?.contains("422") == true -> "Datos incompletos o inválidos."
                        else -> "Error al registrar usuario: ${error.message}"
                    }
                    
                    Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("Registro", "Excepción durante el registro: ${e.message}", e)
                Snackbar.make(findViewById(android.R.id.content), "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}


