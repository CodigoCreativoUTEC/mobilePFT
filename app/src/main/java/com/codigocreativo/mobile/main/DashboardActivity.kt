package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.MarcasActivity
import com.codigocreativo.mobile.features.modelo.ModelosActivity
import com.codigocreativo.mobile.features.proveedores.ProveedoresActivity
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipoActivity

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


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Inicializa cada CardView
        marcasCard = findViewById(R.id.marcasCard)
        usuarioCard = findViewById(R.id.usuarioCard)
        proveedorCard = findViewById(R.id.proveedorCard)
        equiposCard = findViewById(R.id.equiposCard)
        tipoEquiposCard = findViewById(R.id.tipoequiposCard)
        modelosCard = findViewById(R.id.modelosCard)

        // setupDrawer()

        // Configura los OnClickListener para cada CardView
        marcasCard.setOnClickListener {
            val intent = Intent(this, MarcasActivity::class.java)
            startActivity(intent)
        }

        usuarioCard.setOnClickListener {
            //  val intent = Intent(this, UsuariosActivity::class.java)
            startActivity(intent)
        }

        proveedorCard.setOnClickListener {
            val intent = Intent(this, ProveedoresActivity::class.java)
            startActivity(intent)
        }

        equiposCard.setOnClickListener {
            // val intent = Intent(this, EquiposActivity::class.java)
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
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    // Manejar navegación a la actividad 1
                }

                R.id.nav_logout -> {
                    // Manejar navegación a la actividad 2
                }

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val menuButton: ImageView = findViewById(R.id.imageMenu)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}



