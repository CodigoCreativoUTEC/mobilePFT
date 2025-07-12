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
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
        findViewById<ImageView>(R.id.image_add).setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
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

        val searchView: SearchView = findViewById(R.id.search_view)
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setImageResource(R.drawable.ic_close)
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
                        val errorMessage = when {
                            error.message?.contains("OptimisticLockException") == true -> {
                                // Reload users list to get latest data
                                loadUsuarios(token)
                                "Error de concurrencia: El usuario fue modificado por otro usuario. Los datos han sido actualizados."
                            }
                            error.message?.contains("500") == true -> 
                                "Error interno del servidor. Intenta más tarde."
                            else -> "Error al actualizar el usuario: ${error.message}"
                        }
                        
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            errorMessage,
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
        val searchView: SearchView = findViewById(R.id.search_view)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        // Configuración del spinner de estado
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        // Listener para aplicar filtros cuando cambia el texto de búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Opcional: Puedes manejar la acción de búsqueda aquí si lo deseas
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters() // Aplica los filtros cada vez que cambia el texto
                return true
            }
        })

        // Listener para aplicar filtros cuando se selecciona un estado diferente en el Spinner
        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters() // Aplica los filtros cada vez que se cambia el estado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listener para el evento de cierre del SearchView (opcional)
        searchView.setOnCloseListener {
            applyFilters() // Aplica los filtros cuando se cierra el SearchView
            false
        }
    }

    // Función para aplicar los filtros (actualizada)
    private fun applyFilters() {
        val searchView: SearchView = findViewById(R.id.search_view)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        val nombre = searchView.query.toString().takeIf { it.isNotEmpty() }
        val estado = filterStatus.selectedItem as Estado // Ya es de tipo Estado

        filterUsuarios(nombre, estado) // Aplica los filtros localmente
    }

    // Función para filtrar la lista de usuarios (actualizada)
    private fun filterUsuarios(nombre: String?, estado: Estado) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""
        val statusFilter = estado

        // Filtrar la lista de marcas
        filteredList = usuariosList.filter { usuario->
            val matchesName = usuario.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = usuario.estado == statusFilter

            matchesName && matchesStatus
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}