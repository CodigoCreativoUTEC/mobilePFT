package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // Configura el botón del menú lateral
        setupDrawer()

        // Configura los OnClickListener para cada CardView
        setupCardListeners()

        // Actualiza la información del usuario en el header
        updateUserInfo()
    }

    private fun setupDrawer() {
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

    private fun updateUserInfo() {
        try {
            // Obtener el header view del NavigationView
            val headerView = navigationView.getHeaderView(0)
            val userNameTextView = headerView.findViewById<TextView>(R.id.nav_user_name)
            val userRoleTextView = headerView.findViewById<TextView>(R.id.nav_user_role)

            // Obtener información del usuario desde SessionManager
            val user = SessionManager.getCurrentUser(this)
            if (user != null) {
                val fullName = "${user.nombre} ${user.apellido}"
                userNameTextView.text = fullName
                userRoleTextView.text = user.idPerfil?.nombre ?: "Usuario"
                
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
}

