package com.codigocreativo.mobile.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.features.usuarios.UsuariosApiService
import com.codigocreativo.mobile.features.usuarios.Usuario
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetallePerfilUsuarioActivity : AppCompatActivity() {

    private val dataRepository = DataRepository()
    private var usuarioActual: Usuario? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // UI Elements
    private lateinit var editNombre: EditText
    private lateinit var editApellido: EditText
    private lateinit var editCedula: EditText
    private lateinit var editEmail: EditText
    private lateinit var editFechaNacimiento: EditText
    private lateinit var editNombreUsuario: EditText
    private lateinit var editTelefono: EditText
    private lateinit var tvInstitucion: TextView
    private lateinit var tvPerfil: TextView
    private lateinit var btnSave: Button
    private lateinit var username: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_perfi_usuario)

        // Inicializar elementos de UI
        initializeViews()
        
        // Configurar DatePicker para fecha de nacimiento
        setupDatePicker()

        // Obtener el token de la sesión
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los detalles del usuario usando el token
            cargarDetallesUsuario(token)
        } else {
            Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        editNombre = findViewById(R.id.edit_nombre)
        editApellido = findViewById(R.id.edit_apellido)
        editCedula = findViewById(R.id.edit_cedula)
        editEmail = findViewById(R.id.edit_email)
        editFechaNacimiento = findViewById(R.id.edit_fecha_nacimiento)
        editNombreUsuario = findViewById(R.id.edit_nombre_usuario)
        editTelefono = findViewById(R.id.edit_telefono)
        tvInstitucion = findViewById(R.id.tv_institucion)
        tvPerfil = findViewById(R.id.tv_perfil)
        btnSave = findViewById(R.id.btn_save)
        username = findViewById(R.id.username)

        // Configurar el botón de guardar
        btnSave.setOnClickListener {
            guardarCambios()
        }
    }

    private fun setupDatePicker() {
        editFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            // Si ya hay una fecha, parsearla
            val currentDate = editFechaNacimiento.text.toString()
            if (currentDate.isNotEmpty()) {
                try {
                    calendar.time = dateFormat.parse(currentDate) ?: Date()
                } catch (e: Exception) {
                    Log.e("DetallePerfilUsuarioActivity", "Error parsing date: ${e.message}")
                }
            }

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    editFechaNacimiento.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun cargarDetallesUsuario(token: String) {
        val apiService = RetrofitClient.getClient(token).create(UsuariosApiService::class.java)

        lifecycleScope.launch {
            try {
                // Llamar al repository para obtener los datos del usuario
                val userLog = SessionManager.getUser(this@DetallePerfilUsuarioActivity)
                val emailUserLog = userLog?.email
                
                if (emailUserLog == null) {
                    Snackbar.make(findViewById(R.id.main), "No se pudo obtener el email del usuario", Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                
                val result = dataRepository.obtenerDatos(
                    token = token,
                    apiCall = { apiService.buscarUsuarioPorEmail("Bearer $token", emailUserLog) }
                )

                result.onSuccess { usuario ->
                    usuarioActual = usuario
                    // Actualizar la interfaz con los datos del usuario
                    actualizarInterfazUsuario(usuario)
                }.onFailure { error ->
                    Log.e("DetallePerfilUsuarioActivity", "Error al cargar los datos del usuario: ${error.message}")
                    Snackbar.make(findViewById(R.id.main), "Error al cargar los datos del usuario: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetallePerfilUsuarioActivity", "Error inesperado: ${e.message}", e)
                Snackbar.make(findViewById(R.id.main), "Error inesperado al cargar los datos del usuario", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun actualizarInterfazUsuario(usuario: Usuario) {
        // Configurar los campos con los datos del usuario
        editNombre.setText(usuario.nombre)
        editApellido.setText(usuario.apellido)
        editCedula.setText(usuario.cedula)
        editEmail.setText(usuario.email)
        editFechaNacimiento.setText(usuario.fechaNacimiento)
        editNombreUsuario.setText(usuario.nombreUsuario)
        
        // Configurar teléfono (tomar el primero si existe)
        if (!usuario.usuariosTelefonos.isNullOrEmpty()) {
            editTelefono.setText(usuario.usuariosTelefonos[0].numero)
        } else {
            editTelefono.setText("")
        }
        
        // Configurar institución y perfil (solo lectura)
        tvInstitucion.text = usuario.idInstitucion.nombre
        tvPerfil.text = usuario.idPerfil.nombrePerfil
        
        // Configurar nombre de usuario en el header
        username.text = usuario.nombreUsuario
    }

    private fun guardarCambios() {
        val token = SessionManager.getToken(this)
        if (token == null || usuarioActual == null) {
            Snackbar.make(findViewById(R.id.main), "Error: No se puede guardar los cambios", Snackbar.LENGTH_LONG).show()
            return
        }

        // Validar campos obligatorios
        if (!validarCampos()) {
            return
        }

        val telefonoEditado = editTelefono.text.toString().trim()
        val listaTelefonos = if (telefonoEditado.isNotEmpty()) {
            listOf(com.codigocreativo.mobile.features.usuarios.Telefono(
                id = usuarioActual?.usuariosTelefonos?.firstOrNull()?.id ?: 0,
                numero = telefonoEditado
            ))
        } else {
            emptyList()
        }

        val usuarioActualizado = usuarioActual!!.copy(
            nombre = editNombre.text.toString().trim(),
            apellido = editApellido.text.toString().trim(),
            cedula = editCedula.text.toString().trim(),
            email = editEmail.text.toString().trim(),
            fechaNacimiento = editFechaNacimiento.text.toString().trim(),
            nombreUsuario = editNombreUsuario.text.toString().trim(),
            usuariosTelefonos = listaTelefonos
        )

        // Mostrar indicador de carga
        btnSave.isEnabled = false
        btnSave.text = "Guardando..."

        val apiService = RetrofitClient.getClient(token).create(UsuariosApiService::class.java)

        lifecycleScope.launch {
            try {
                val result = dataRepository.obtenerDatos(
                    token = token,
                    apiCall = { apiService.actualizar("Bearer $token", usuarioActualizado) }
                )

                result.onSuccess {
                    Snackbar.make(findViewById(R.id.main), "Datos actualizados correctamente", Snackbar.LENGTH_LONG).show()
                    usuarioActual = usuarioActualizado
                    // Actualizar el nombre de usuario en el header
                    username.text = usuarioActualizado.nombreUsuario
                }.onFailure { error ->
                    Log.e("DetallePerfilUsuarioActivity", "Error al actualizar usuario: ${error.message}")
                    Snackbar.make(findViewById(R.id.main), "Error al actualizar los datos: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetallePerfilUsuarioActivity", "Error inesperado al actualizar: ${e.message}", e)
                Snackbar.make(findViewById(R.id.main), "Error inesperado al actualizar los datos", Snackbar.LENGTH_LONG).show()
            } finally {
                // Restaurar botón
                btnSave.isEnabled = true
                btnSave.text = "Guardar Cambios"
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = editNombre.text.toString().trim()
        val apellido = editApellido.text.toString().trim()
        val cedula = editCedula.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val fechaNacimiento = editFechaNacimiento.text.toString().trim()
        val nombreUsuario = editNombreUsuario.text.toString().trim()

        if (nombre.isEmpty()) {
            editNombre.error = "El nombre es obligatorio"
            return false
        }

        if (apellido.isEmpty()) {
            editApellido.error = "El apellido es obligatorio"
            return false
        }

        if (cedula.isEmpty()) {
            editCedula.error = "La cédula es obligatoria"
            return false
        }

        if (email.isEmpty()) {
            editEmail.error = "El email es obligatorio"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "El email no tiene un formato válido"
            return false
        }

        if (fechaNacimiento.isEmpty()) {
            editFechaNacimiento.error = "La fecha de nacimiento es obligatoria"
            return false
        }

        if (nombreUsuario.isEmpty()) {
            editNombreUsuario.error = "El nombre de usuario es obligatorio"
            return false
        }

        return true
    }
}
