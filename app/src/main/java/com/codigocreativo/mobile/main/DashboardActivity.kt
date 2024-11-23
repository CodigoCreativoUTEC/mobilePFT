package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.equipos.EquiposActivity
import com.codigocreativo.mobile.features.marca.MarcasActivity
import com.codigocreativo.mobile.features.modelo.ModelosActivity
import com.codigocreativo.mobile.features.proveedores.ProveedoresActivity
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipoActivity
import com.codigocreativo.mobile.features.usuarios.UsuariosActivity



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
    }

    private fun setupDrawer() {
        // Configura el listener para las opciones del menú
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_view_profile -> {
                    openProfileScreen() // Abrir perfil
                    true
                }
                R.id.nav_logout -> {
                    logout() // Cerrar sesión
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

    private fun setupCardListeners() {
        marcasCard.setOnClickListener {
            val intent = Intent(this, MarcasActivity::class.java)
            startActivity(intent)
        }

        usuarioCard.setOnClickListener {
            val intent = Intent(this, UsuariosActivity::class.java)
            startActivity(intent)
        }

        proveedorCard.setOnClickListener {
            val intent = Intent(this, ProveedoresActivity::class.java)
            startActivity(intent)
        }

        equiposCard.setOnClickListener {
            val intent = Intent(this, EquiposActivity::class.java)
            startActivity(intent)
        }

        tipoEquiposCard.setOnClickListener {
            val intent = Intent(this, TipoEquipoActivity::class.java)
            startActivity(intent)
        }

        modelosCard.setOnClickListener {
            val intent = Intent(this, ModelosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openProfileScreen() {
        // Abre el fragmento del perfil del usuario
//        val fragment = PerfilUsuarioFragment.newInstance()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.main_content, fragment)
//            .addToBackStack(null)
//            .commit()
        val intent = Intent(this, DetallePerfilUsuarioActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        // Limpia la sesión del usuario
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navega a la pantalla de login
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

