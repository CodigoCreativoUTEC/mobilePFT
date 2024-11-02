package com.codigocreativo.mobile.features.usuarios

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.proveedores.DetalleProveedorFragment
import com.codigocreativo.mobile.features.proveedores.IngresarProveedorFragment
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.proveedores.ProveedorAdapter
import com.codigocreativo.mobile.features.proveedores.ProveedoresApiService
import com.codigocreativo.mobile.main.DashboardActivity
import com.codigocreativo.mobile.main.Registro
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class UsuariosActivity : AppCompatActivity() {

    private lateinit var adapter: UsuariosAdapter
    private lateinit var recyclerView: RecyclerView
    private var usuariosList = mutableListOf<Usuario>() // Lista dinámica de usuarios cargados desde el API
    private var filteredList = mutableListOf<Usuario>()
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuariosAdapter(filteredList, this) { usuario ->
            showDetalleUsuarioFragment(usuario)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los proveedores desde el API
            loadUsuarios(token)
        } else {
            Snackbar.make(
                findViewById(R.id.main),
                "Token no encontrado, por favor inicia sesión",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar Usuario
        findViewById<Button>(R.id.btn_ingresar).setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
        // Action listener de Volver al Menú
        findViewById<Button>(R.id.btn_volver_menu).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val usuario = adapter.usuarioList[position]

                // Show confirmation dialog
                AlertDialog.Builder(this@UsuariosActivity).apply {
                    setTitle("Confirmar baja")
                    setMessage("¿Estás seguro que deseas dar de baja al usuario ${usuario.nombre}?")
                    setPositiveButton("Si") { _, _ ->
                        val token = SessionManager.getToken(this@UsuariosActivity)
                        if (token != null) {
                            val retrofit = RetrofitClient.getClient(token)
                            val apiService = retrofit.create(UsuariosApiService::class.java)

                            lifecycleScope.launch {
                                val result = dataRepository.guardarDatos(
                                    token = token,
                                    apiCall = { apiService.eliminarUsuario("Bearer $token",
                                        usuario.id!!
                                    ) }
                                )

                                result.onSuccess {
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Usuario dado de baja correctamente",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    loadUsuarios(token)
                                }.onFailure { error ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error al dar de baja al usuario: ${error.message}",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    Log.e("UsuariosActivity", "Error al dar de baja al usuario: ${error.message}")
                                }
                            }
                        } else {
                            Snackbar.make(
                                findViewById(R.id.main),
                                "Token no encontrado, por favor inicia sesión",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                    setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                        adapter.notifyItemChanged(position)
                    }
                    create()
                    show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun loadUsuarios(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(UsuariosApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarUsuarios("Bearer $token") }
            )

            result.onSuccess { usuarios ->
                usuariosList.clear()
                usuariosList.addAll(usuarios) // Agregar los usuarios obtenidos
                adapter.updateList(usuariosList) // Actualizar el RecyclerView con los usuarios
            }.onFailure { error ->
                Log.e("UsuariosActivity", "Error al cargar los usuarios: ${error.message}")
            }
        }
    }

    private fun showDetalleUsuarioFragment(usuario: Usuario) {
        val fragment = DetalleUsuarioFragment(usuario) { updatedUsuario ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(UsuariosApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.actualizar("Bearer $token", updatedUsuario) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Usuario actualizado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadUsuarios(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar el usuario: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("UsuariosActivity", "Error al actualizar el usuario: ${error.message}\nPayload: ${updatedUsuario}")
                    }
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.main),
                    "Token no encontrado, por favor inicia sesión",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        fragment.show(supportFragmentManager, "DetalleUsuarioFragment")
    }

    private fun setupFilters() {
        val filterName: EditText = findViewById(R.id.filter_name)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        // Configuración del spinner de estado
        val statusAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        // Listener para aplicar filtros
        val applyFilters = {
            val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)
            val nombre = filterName.text.toString().takeIf { it.isNotEmpty() }
            val estado = (filterStatus.selectedItem as Estado).name
            if (token != null) {
                loadUsuarios(token = token, nombre = nombre, estado = estado)
            }
        }

        filterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Método de filtro para usuarios
    private fun filterUsuarios() {
        val nameFilter =
            findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de usuarios
        filteredList = usuariosList.filter { usuarios ->
            usuarios.nombre.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    (statusFilter == Estado.ACTIVO || statusFilter == Estado.INACTIVO || usuarios.estado == statusFilter)
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}