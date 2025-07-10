package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.equipos.EquiposActivity
import com.codigocreativo.mobile.features.marca.MarcasActivity
import com.codigocreativo.mobile.features.modelo.ModelosActivity
import com.codigocreativo.mobile.features.proveedores.ProveedoresActivity
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipoActivity
import com.codigocreativo.mobile.features.usuarios.UsuariosActivity
import com.codigocreativo.mobile.utils.SessionManager
import com.codigocreativo.mobile.network.User
import com.codigocreativo.mobile.main.AccessDeniedActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var marcasCard: CardView
    private lateinit var usuarioCard: CardView
    private lateinit var proveedorCard: CardView
    private lateinit var equiposCard: CardView
    private lateinit var tipoEquiposCard: CardView
    private lateinit var modelosCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inicializa el DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Inicializa cada CardView
        marcasCard = findViewById(R.id.marcasCard)
        usuarioCard = findViewById(R.id.usuarioCard)
        proveedorCard = findViewById(R.id.proveedorCard)
        equiposCard = findViewById(R.id.equiposCard)
        tipoEquiposCard = findViewById(R.id.tipoequiposCard)
        modelosCard = findViewById(R.id.modelosCard)

        // Verificar permisos del usuario
        if (!hasAnyAccess()) {
            // Si no tiene ningún permiso, mostrar pantalla de acceso denegado
            showAccessDeniedScreen()
            return
        }

        // Configura el botón del menú lateral
        setupDrawer()

        // Configura los OnClickListener para cada CardView
        setupCardListeners()

        // Configura la visibilidad de las tarjetas según los permisos
        setupCardVisibility()

        // Actualiza la información del usuario en el header
        updateUserInfo()
    }

    private fun setupDrawer() {
        // Configura la visibilidad de los elementos del menú según los permisos
        setupMenuVisibility()

        // Configura el listener para las opciones del menú
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_users -> {
                    openUsersScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_equipos -> {
                    openEquiposScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_tipo_equipo -> {
                    openTipoEquipoScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_marcas -> {
                    openMarcasScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_modelos -> {
                    openModelosScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_proveedores -> {
                    openProveedoresScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_view_profile -> {
                    openProfileScreen()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        // Configura el botón de menú para abrir el drawer
        val menuButton: ImageView = findViewById(R.id.imageMenu)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupMenuVisibility() {
        val menu = navigationView.menu
        
        if (hasAdminAccess()) {
            // Admin: acceso completo a todas las opciones
            menu.findItem(R.id.nav_users).isVisible = true
            menu.findItem(R.id.nav_equipos).isVisible = true
            menu.findItem(R.id.nav_tipo_equipo).isVisible = true
            menu.findItem(R.id.nav_marcas).isVisible = true
            menu.findItem(R.id.nav_modelos).isVisible = true
            menu.findItem(R.id.nav_proveedores).isVisible = true
        } else if (hasEquiposAccess()) {
            // Ingeniero Biomédico, Tecnólogo, Técnico: solo acceso a Equipos
            menu.findItem(R.id.nav_users).isVisible = false
            menu.findItem(R.id.nav_equipos).isVisible = true
            menu.findItem(R.id.nav_tipo_equipo).isVisible = false
            menu.findItem(R.id.nav_marcas).isVisible = false
            menu.findItem(R.id.nav_modelos).isVisible = false
            menu.findItem(R.id.nav_proveedores).isVisible = false
        }
    }

    private fun updateUserInfo() {
        try {
            // Obtener el header view del NavigationView
            val headerView = navigationView.getHeaderView(0)
            val userNameTextView = headerView.findViewById<TextView>(R.id.nav_user_name)
            val userRoleTextView = headerView.findViewById<TextView>(R.id.nav_user_role)

            // Obtener información del usuario desde SessionManager
            val user = SessionManager.getUser(this)
            if (user != null) {
                val fullName = "${user.nombre} ${user.apellido}"
                userNameTextView.text = fullName
                userRoleTextView.text = user.idPerfil.nombrePerfil
                
                Log.d("DashboardActivity", "Usuario actualizado en header: $fullName")
            } else {
                userNameTextView.text = "Usuario"
                userRoleTextView.text = "Sistema"
                Log.w("DashboardActivity", "No se pudo obtener información del usuario")
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error actualizando información del usuario: ${e.message}")
        }
    }

    private fun setupCardListeners() {
        marcasCard.setOnClickListener {
            openMarcasScreen()
        }

        usuarioCard.setOnClickListener {
            openUsersScreen()
        }

        proveedorCard.setOnClickListener {
            openProveedoresScreen()
        }

        equiposCard.setOnClickListener {
            openEquiposScreen()
        }

        tipoEquiposCard.setOnClickListener {
            openTipoEquipoScreen()
        }

        modelosCard.setOnClickListener {
            openModelosScreen()
        }
    }

    private fun setupCardVisibility() {
        if (hasAdminAccess()) {
            // Admin: acceso completo a todas las tarjetas
            marcasCard.visibility = View.VISIBLE
            usuarioCard.visibility = View.VISIBLE
            proveedorCard.visibility = View.VISIBLE
            equiposCard.visibility = View.VISIBLE
            tipoEquiposCard.visibility = View.VISIBLE
            modelosCard.visibility = View.VISIBLE
        } else if (hasEquiposAccess()) {
            // Ingeniero Biomédico, Tecnólogo, Técnico: solo acceso a Equipos
            marcasCard.visibility = View.GONE
            usuarioCard.visibility = View.GONE
            proveedorCard.visibility = View.GONE
            equiposCard.visibility = View.VISIBLE
            tipoEquiposCard.visibility = View.GONE
            modelosCard.visibility = View.GONE
        }
    }

    private fun openUsersScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Usuarios")
        val intent = Intent(this, UsuariosActivity::class.java)
        startActivity(intent)
    }

    private fun openEquiposScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Equipos")
        val intent = Intent(this, EquiposActivity::class.java)
        startActivity(intent)
    }

    private fun openTipoEquipoScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Tipos de Equipo")
        val intent = Intent(this, TipoEquipoActivity::class.java)
        startActivity(intent)
    }

    private fun openMarcasScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Marcas")
        val intent = Intent(this, MarcasActivity::class.java)
        startActivity(intent)
    }

    private fun openModelosScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Modelos")
        val intent = Intent(this, ModelosActivity::class.java)
        startActivity(intent)
    }

    private fun openProveedoresScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Proveedores")
        val intent = Intent(this, ProveedoresActivity::class.java)
        startActivity(intent)
    }

    private fun openProfileScreen() {
        Log.d("DashboardActivity", "Abriendo pantalla de Perfil")
        val intent = Intent(this, DetallePerfilUsuarioActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        Log.d("DashboardActivity", "Cerrando sesión")
        
        // Limpia la sesión del usuario usando SessionManager
        SessionManager.clearSession(this)
        
        // Navega a la pantalla de login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Verifica si el usuario tiene acceso completo al dashboard (Admin)
     */
    private fun hasAdminAccess(): Boolean {
        try {
            val user = SessionManager.getUser(this)
            if (user != null) {
                val profileName = user.idPerfil.nombrePerfil
                Log.d("DashboardActivity", "Verificando permisos de administrador para perfil: $profileName")
                
                return profileName.equals("Admin", ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error verificando permisos de administrador: ${e.message}")
        }
        return false
    }

    /**
     * Verifica si el usuario tiene acceso solo a Equipos
     */
    private fun hasEquiposAccess(): Boolean {
        try {
            val user = SessionManager.getUser(this)
            if (user != null) {
                val profileName = user.idPerfil.nombrePerfil
                Log.d("DashboardActivity", "Verificando permisos de equipos para perfil: $profileName")
                
                val equiposProfiles = listOf(
                    "Ingeniero Biomédico",
                    "Tecnólogo",
                    "Técnico"
                )
                
                return equiposProfiles.any { it.equals(profileName, ignoreCase = true) }
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error verificando permisos de equipos: ${e.message}")
        }
        return false
    }

    /**
     * Verifica si el usuario tiene algún tipo de acceso
     */
    private fun hasAnyAccess(): Boolean {
        return hasAdminAccess() || hasEquiposAccess()
    }

    /**
     * Muestra la pantalla de acceso denegado
     */
    private fun showAccessDeniedScreen() {
        Log.d("DashboardActivity", "Mostrando pantalla de acceso denegado")
        val intent = Intent(this, AccessDeniedActivity::class.java)
        startActivity(intent)
        finish() // Cierra esta actividad para que no se pueda volver atrás
    }
}

