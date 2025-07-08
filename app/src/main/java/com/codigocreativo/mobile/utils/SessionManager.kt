package com.codigocreativo.mobile.utils

import android.content.Context
import com.codigocreativo.mobile.features.usuarios.Usuario
import com.codigocreativo.mobile.network.User
import com.google.gson.Gson


object SessionManager {
    private const val PREF_NAME = "app_prefs"
    private const val TOKEN_KEY = "jwt_token"
    private const val USER_KEY = "USER_KEY"


    fun saveLoggedUser(context: Context, usuario: Usuario) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(usuario)
        editor.putString(USER_KEY, json)
        editor.apply()
    }

    fun getLoggedUser(context: Context): Usuario? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(USER_KEY, null)
        return json?.let { Gson().fromJson(it, Usuario::class.java) }
    }


    // Guardar el token y el objeto usuario
    fun saveSessionData(context: Context, token: String, user: User) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Guardar el JWT
        editor.putString(TOKEN_KEY, token)

        // Guardar el objeto usuario como JSON
        val userJson = Gson().toJson(user)
        editor.putString(USER_KEY, userJson)

        editor.apply()
    }

    // Obtener el token
    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(TOKEN_KEY, null)
    }

    // Obtener el objeto usuario
    fun getUser(context: Context): User? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userJson = sharedPref.getString(USER_KEY, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    // Convertir User a Usuario
    fun getUserAsUsuario(context: Context): com.codigocreativo.mobile.features.usuarios.Usuario? {
        val user = getUser(context) ?: return null
        
        return com.codigocreativo.mobile.features.usuarios.Usuario(
            id = user.id,
            cedula = user.cedula,
            email = user.email,
            contrasenia = user.contrasenia ?: "",
            fechaNacimiento = user.fechaNacimiento,
            estado = user.estado,
            nombre = user.nombre,
            apellido = user.apellido,
            nombreUsuario = user.nombreUsuario,
            idInstitucion = com.codigocreativo.mobile.features.institucion.Institucion(
                id = user.idInstitucion.id,
                nombre = user.idInstitucion.nombre
            ),
            idPerfil = com.codigocreativo.mobile.features.perfiles.Perfil(
                id = user.idPerfil.id,
                nombrePerfil = user.idPerfil.nombrePerfil,
                estado = user.idPerfil.estado
            ),
            usuariosTelefonos = user.usuariosTelefonos.map { telefono ->
                com.codigocreativo.mobile.features.usuarios.Telefono(
                    id = telefono.id,
                    numero = telefono.numero
                )
            }
        )
    }

    // Borrar los datos de sesión
    fun clearSession(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
}