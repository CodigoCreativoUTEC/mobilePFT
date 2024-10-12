package com.codigocreativo.mobile.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.MarcasActivity
import com.codigocreativo.mobile.features.modelo.ModelosActivity
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipoActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Configurar la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Agregar el botón hamburguesa
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Manejar las selecciones del menú
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_users -> {
                    Toast.makeText(this, "Usuarios seleccionados", Toast.LENGTH_SHORT).show()

                }
                R.id.nav_brands -> {
                    Toast.makeText(this, "Marcas seleccionadas", Toast.LENGTH_SHORT).show()
                    // Navegar a otra actividad
                    val intent = Intent(this, MarcasActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_models -> {
                    Toast.makeText(this, "Modelos seleccionados", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ModelosActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_tipoEquipos -> {
                    Toast.makeText(this, "Tipo de equipos seleccionados", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TipoEquipoActivity::class.java)
                    startActivity(intent)
                }
            }
            // Cerrar el drawer después de hacer clic
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(navigationView)
        }
        return super.onOptionsItemSelected(item)
    }
}

