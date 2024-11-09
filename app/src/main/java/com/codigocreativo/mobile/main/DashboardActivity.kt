package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
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

    private fun setupDrawer() {
        // Sincroniza el botón del drawer con la ActionBar (opcional)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configura el listener para las opciones del menú
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_view_profile -> {
                    // Lógica para ver perfil
                    openProfileScreen()
                }
                R.id.nav_logout -> {
                    // Lógica para cerrar sesión
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Configura el botón de menú para abrir el drawer
        val menuButton: ImageView = findViewById(R.id.imageMenu)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun openProfileScreen() {
        // Implementa la lógica para abrir la pantalla de perfil
      //  val intent = Intent(this, ProfileActivity::class.java)
      //  startActivity(intent)
    }

    private fun logout() {
        // Implementa la lógica para cerrar sesión
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        // Ejemplo: ir a la pantalla de login o limpiar datos de sesión
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
