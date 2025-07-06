package com.codigocreativo.mobile.features.proveedores

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.main.DashboardActivity
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class ProveedoresActivity : AppCompatActivity() {

    private lateinit var adapter: ProveedorAdapter
    private lateinit var recyclerView: RecyclerView
    private var proveedoresList = mutableListOf<Proveedor>() // Lista dinámica de proveedores cargados desde el API
    private var filteredList = mutableListOf<Proveedor>()
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proveedores)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProveedorAdapter(filteredList, this) { proveedor ->
            showDetalleProveedorFragment(proveedor)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los proveedores desde el API
            loadProveedores(token)
        } else {
            Snackbar.make(
                findViewById(R.id.main),
                "Token no encontrado, por favor inicia sesión",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar Proveedor
        findViewById<ImageView>(R.id.image_add).setOnClickListener {
            val bottomSheetFragment = IngresarProveedorFragment { proveedor ->
                if (token != null) {
                    val retrofit = RetrofitClient.getClient(token)
                    val apiService = retrofit.create(ProveedoresApiService::class.java)

                    lifecycleScope.launch {
                        val result = dataRepository.guardarDatos(
                            token = token,
                            apiCall = {
                                apiService.crearProveedor("Bearer $token", proveedor)
                            })

                        result.onSuccess {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Proveedor ingresado correctamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadProveedores(token)
                        }.onFailure { error ->
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Error al ingresar el proveedor: ${error.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e("ProveedoresActivity", "Error al ingresar el proveedor: ${error.message}\nPayload: ${proveedor}")
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
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
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
                val proveedor = adapter.proveedorList[position]

                // Show confirmation dialog
                AlertDialog.Builder(this@ProveedoresActivity).apply {
                    setTitle("Confirmar baja")
                    setMessage("¿Estás seguro que deseas dar de baja al proveedor ${proveedor.nombre}?")
                    setPositiveButton("Si") { _, _ ->
                        val token = SessionManager.getToken(this@ProveedoresActivity)
                        if (token != null) {
                            val retrofit = RetrofitClient.getClient(token)
                            val apiService = retrofit.create(ProveedoresApiService::class.java)

                            lifecycleScope.launch {
                                val result = dataRepository.guardarDatos(
                                    token = token,
                                    apiCall = { apiService.eliminarProveedor("Bearer $token",
                                        proveedor.idProveedor!!
                                    ) }
                                )

                                result.onSuccess {
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Proveedor dado de baja correctamente",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    loadProveedores(token)
                                }.onFailure { error ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error al dar de baja al proveedor: ${error.message}",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    Log.e("ProveedoresActivity", "Error al dar de baja al proveedor: ${error.message}")
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

    private fun loadProveedores(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(ProveedoresApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.buscarProveedores("Bearer $token", nombre, estado) }
            )

            result.onSuccess { proveedores ->
                Log.d("ProveedoresActivity", "Datos recibidos del servidor: ${proveedores.size} proveedores")
                proveedores.forEach { proveedor ->
                    Log.d("ProveedoresActivity", "Proveedor del servidor: ${proveedor.nombre}, Estado: ${proveedor.estado}")
                }
                
                proveedoresList.clear()
                proveedoresList.addAll(proveedores) // Agregar los proveedores obtenidos
                // Actualizar directamente el adapter con los datos filtrados del servidor
                adapter.updateList(proveedoresList)
            }.onFailure { error ->
                Log.e("ProveedoresActivity", "Error al cargar los proveedores: ${error.message}")
            }
        }
    }

    private fun showDetalleProveedorFragment(proveedor: Proveedor) {
        val fragment = DetalleProveedorFragment(proveedor) { updatedProveedor ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(ProveedoresApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.actualizar("Bearer $token", updatedProveedor) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Proveedor actualizado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadProveedores(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar el proveedor: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("ProveedoresActivity", "Error al actualizar el proveedor: ${error.message}\nPayload: ${updatedProveedor}")
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

        fragment.show(supportFragmentManager, "DetalleProveedorFragment")
    }

    private fun setupFilters() {
        val searchView: SearchView = findViewById(R.id.search_view)
        val filterStatus: AutoCompleteTextView = findViewById(R.id.filter_status)

        // Configuración del AutoCompleteTextView de estado con opción "Todos"
        val estadosConTodos = listOf("Todos") + Estado.values().map { it.name }
        val statusAdapter = ArrayAdapter(this, R.layout.item_dropdown_minimal, estadosConTodos)
        filterStatus.setAdapter(statusAdapter)
        filterStatus.setText("Todos", false) // Establecer valor por defecto
        
        // Configurar para mostrar el dropdown al hacer clic
        filterStatus.setOnClickListener {
            filterStatus.showDropDown()
        }
        
        // Configurar el ancho del dropdown
        filterStatus.dropDownWidth = (resources.displayMetrics.widthPixels * 0.5).toInt()

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

        // Listener para aplicar filtros cuando se selecciona un estado diferente en el AutoCompleteTextView
        filterStatus.setOnItemClickListener { _, _, position, _ ->
            applyFilters() // Aplica los filtros cada vez que se cambia el estado
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
        val filterStatus: AutoCompleteTextView = findViewById(R.id.filter_status)

        val nombre = searchView.query.toString().takeIf { it.isNotEmpty() }
        val estadoSeleccionado = filterStatus.text.toString()
        val estado = if (estadoSeleccionado == "Todos") null else Estado.valueOf(estadoSeleccionado)

        Log.d("ProveedoresActivity", "Aplicando filtros - Nombre: '$nombre', Estado seleccionado: '$estadoSeleccionado', Estado: $estado")

        // Obtener el token y cargar proveedores con filtros
        val token = SessionManager.getToken(this)
        if (token != null) {
            loadProveedores(token, nombre, estado?.name)
        }
    }

    // Función para filtrar la lista de proveedores(actualizada)
    private fun filterProveedores(nombre: String?, estado: Estado?) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""

        // Filtrar la lista de proveedores
        filteredList = proveedoresList.filter { proveedor ->
            val matchesName = proveedor.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = estado == null || proveedor.estado == estado

            Log.d("ProveedoresActivity", "Filtrando: ${proveedor.nombre} - matchesName: $matchesName, matchesStatus: $matchesStatus (estado: ${proveedor.estado}, filtro: $estado)")

            matchesName && matchesStatus
        }.toMutableList()

        Log.d("ProveedoresActivity", "Resultado filtrado: ${filteredList.size} proveedores")
        filteredList.forEach { proveedor ->
            Log.d("ProveedoresActivity", "Proveedor filtrado: ${proveedor.nombre}, Estado: ${proveedor.estado}")
        }

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}