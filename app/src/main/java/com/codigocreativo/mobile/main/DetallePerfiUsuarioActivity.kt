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
        // Deshabilitar edición del nombre de usuario
        editNombreUsuario.isEnabled = false
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
                    Snackbar.make(findViewById(android.R.id.content), "No se pudo obtener el email del usuario", Snackbar.LENGTH_LONG).show()
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
                    
                    // Log the full error message for debugging
                    Log.d("DetallePerfilUsuarioActivity", "Error completo: ${error.message}")
                    
                    val errorMessage = when {
                        error.message?.contains("403") == true || 
                        error.message?.contains("Acceso denegado") == true ||
                        error.message?.contains("No tienes permisos") == true -> {
                            // Show basic profile info from session data when server denies access
                            Log.d("DetallePerfilUsuarioActivity", "Detectado error 403, mostrando perfil básico")
                            mostrarPerfilBasico()
                            
                            // Check user profile to show appropriate message
                            val user = SessionManager.getUser(this@DetallePerfilUsuarioActivity)
                            val profileName = user?.idPerfil?.nombrePerfil ?: ""
                            val allowedProfiles = listOf("Administrador", "Aux administrativo", "Ingeniero Biomédico", "Tecnólogo", "Técnico")
                            
                            if (allowedProfiles.any { it.equals(profileName, ignoreCase = true) }) {
                                "Tu perfil debería ser editable. Si ves este mensaje, contacta al administrador del sistema."
                            } else {
                                "No tienes permisos para modificar tu perfil desde el servidor. Mostrando información básica."
                            }
                        }
                        error.message?.contains("500") == true -> 
                            "Error interno del servidor. Intenta más tarde."
                        else -> {
                            Log.d("DetallePerfilUsuarioActivity", "Error no reconocido, mostrando perfil básico por defecto")
                            mostrarPerfilBasico()
                            "Error al cargar los datos del usuario. Mostrando información básica."
                        }
                    }
                    Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetallePerfilUsuarioActivity", "Error inesperado: ${e.message}", e)
                Snackbar.make(findViewById(android.R.id.content), "Error inesperado al cargar los datos del usuario", Snackbar.LENGTH_LONG).show()
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
            Snackbar.make(findViewById(android.R.id.content), "Error: No se puede guardar los cambios", Snackbar.LENGTH_LONG).show()
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
            usuariosTelefonos = listaTelefonos,
            contrasenia = usuarioActual?.contrasenia ?: ""
        )

        // Mostrar indicador de carga
        btnSave.isEnabled = false
        btnSave.text = "Guardando..."

        val apiService = RetrofitClient.getClient(token).create(UsuariosApiService::class.java)

        lifecycleScope.launch {
            try {
                val result = dataRepository.obtenerDatos(
                    token = token,
                    apiCall = { apiService.modificarPropioUsuario("Bearer $token", usuarioActualizado) }
                )

                result.onSuccess {
                    Snackbar.make(findViewById(android.R.id.content), "Datos actualizados correctamente", Snackbar.LENGTH_LONG).show()
                    usuarioActual = usuarioActualizado
                    // Actualizar el nombre de usuario en el header
                    username.text = usuarioActualizado.nombreUsuario
                }.onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("OptimisticLockException") == true -> {
                            // Reload user data to get latest information
                            cargarDetallesUsuario(token)
                            "Error de concurrencia: Los datos fueron modificados por otro usuario. Los datos han sido actualizados."
                        }
                        error.message?.contains("403") == true || error.message?.contains("Acceso denegado") == true -> 
                            "No tienes permisos para modificar tu perfil. Contacta al administrador."
                        error.message?.contains("500") == true -> 
                            "Error interno del servidor. Intenta más tarde."
                        else -> "Error al actualizar los datos: ${error.message}"
                    }
                    
                    Log.e("DetallePerfilUsuarioActivity", "Error al actualizar usuario: ${error.message}")
                    Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetallePerfilUsuarioActivity", "Error inesperado al actualizar: ${e.message}", e)
                Snackbar.make(findViewById(android.R.id.content), "Error inesperado al actualizar los datos", Snackbar.LENGTH_LONG).show()
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
    
    /**
     * Muestra información básica del perfil usando datos de la sesión
     * cuando el servidor deniega acceso
     */
    private fun mostrarPerfilBasico() {
        try {
            Log.d("DetallePerfilUsuarioActivity", "Iniciando mostrarPerfilBasico()")
            val user = SessionManager.getUser(this)
            if (user != null) {
                Log.d("DetallePerfilUsuarioActivity", "Usuario obtenido de sesión: ${user.nombreUsuario}")
                
                // Populate fields with session data
                editNombre.setText(user.nombre)
                editApellido.setText(user.apellido)
                editCedula.setText(user.cedula)
                editEmail.setText(user.email)
                editFechaNacimiento.setText(user.fechaNacimiento)
                editNombreUsuario.setText(user.nombreUsuario)
                
                // Set phone from session data
                if (!user.usuariosTelefonos.isNullOrEmpty()) {
                    editTelefono.setText(user.usuariosTelefonos[0].numero)
                    Log.d("DetallePerfilUsuarioActivity", "Teléfono configurado: ${user.usuariosTelefonos[0].numero}")
                } else {
                    editTelefono.setText("")
                    Log.d("DetallePerfilUsuarioActivity", "No hay teléfonos en la sesión")
                }
                
                // Set institution and profile from session data
                tvInstitucion.text = user.idInstitucion.nombre
                tvPerfil.text = user.idPerfil.nombrePerfil
                
                // Set username in header
                username.text = user.nombreUsuario
                
                // Disable editing since we can't save changes
                habilitarEdicion(false)
                
                Log.d("DetallePerfilUsuarioActivity", "Perfil básico configurado exitosamente para usuario: ${user.nombreUsuario}")
            } else {
                Log.e("DetallePerfilUsuarioActivity", "No se pudo obtener información del usuario de la sesión")
                Snackbar.make(findViewById(android.R.id.content), "No se pudo obtener información del usuario", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("DetallePerfilUsuarioActivity", "Error mostrando perfil básico: ${e.message}", e)
            Snackbar.make(findViewById(android.R.id.content), "Error al mostrar información del perfil", Snackbar.LENGTH_LONG).show()
        }
    }
    
    /**
     * Habilita o deshabilita la edición de los campos del perfil
     */
    private fun habilitarEdicion(habilitado: Boolean) {
        editNombre.isEnabled = habilitado
        editApellido.isEnabled = habilitado
        editCedula.isEnabled = habilitado
        editEmail.isEnabled = habilitado
        editFechaNacimiento.isEnabled = habilitado
        editNombreUsuario.isEnabled = habilitado
        editTelefono.isEnabled = habilitado
        btnSave.isEnabled = habilitado
        btnSave.text = if (habilitado) "Guardar Cambios" else "Sin permisos de edición"
    }
}
