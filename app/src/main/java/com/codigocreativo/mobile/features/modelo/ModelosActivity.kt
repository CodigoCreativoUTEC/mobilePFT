package com.codigocreativo.mobile.features.modelo

import android.annotation.SuppressLint
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
import com.codigocreativo.mobile.features.marca.DetalleMarcaFragment
import com.codigocreativo.mobile.features.marca.IngresarMarcaFragment
import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.features.marca.MarcaAdapter
import com.codigocreativo.mobile.features.marca.MarcaApiService
import com.codigocreativo.mobile.features.proveedores.DetalleProveedorFragment
import com.codigocreativo.mobile.features.proveedores.IngresarProveedorFragment
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.proveedores.ProveedorAdapter
import com.codigocreativo.mobile.features.proveedores.ProveedoresApiService
import com.codigocreativo.mobile.main.DashboardActivity
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class ModelosActivity : AppCompatActivity() {

    private lateinit var adapter: ModeloAdapter
    private lateinit var recyclerView: RecyclerView
    private var modelosList = mutableListOf<Modelo>() // Lista dinámica de marcas cargados desde el API
    private var filteredList = mutableListOf<Modelo>()
    private val dataRepository = DataRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modelos)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ModeloAdapter(filteredList, this) { modelo ->
            showDetalleModeloFragment(modelo)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los modelos desde el API
            loadModelos(token)
        } else {
            Snackbar.make(
                findViewById(R.id.main),
                "Token no encontrado, por favor inicia sesión",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar Modelo
        findViewById<ImageView>(R.id.image_add).setOnClickListener {
            val bottomSheetFragment = IngresarModeloFragment { modelo ->
                if (token != null) {
                    val retrofit = RetrofitClient.getClient(token)
                    val apiService = retrofit.create(ModeloApiService::class.java)

                    lifecycleScope.launch {
                        val result = dataRepository.guardarDatos(
                            token = token,
                            apiCall = {
                                apiService.crearModelo("Bearer $token", modelo)
                            })

                        result.onSuccess {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Modelo ingresada correctamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadModelos(token)
                        }.onFailure { error ->
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Error al ingresar el modelo: ${error.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e("ModeloActivity", "Error al ingresar el modelo: ${error.message}\nPayload: ${modelo}")
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
                val modelo = adapter.modelosList[position]

                // Show confirmation dialog
                androidx.appcompat.app.AlertDialog.Builder(this@ModelosActivity).apply {
                    setTitle("Confirmar baja")
                    setMessage("¿Estás seguro que deseas dar de baja el modelo ${modelo.nombre}?")
                    setPositiveButton("Si") { _, _ ->
                        val token = SessionManager.getToken(this@ModelosActivity)
                        if (token != null) {
                            val retrofit = RetrofitClient.getClient(token)
                            val apiService = retrofit.create(ModeloApiService::class.java)

                            lifecycleScope.launch {
                                val result = dataRepository.guardarDatos(
                                    token = token,
                                    apiCall = { apiService.eliminarModelo("Bearer $token",
                                        modelo.id!!
                                    ) }
                                )

                                result.onSuccess {
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Modelo dado de baja correctamente",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    loadModelos(token)
                                }.onFailure { error ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error al dar de baja el modelo: ${error.message}",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    Log.e("ModelosActivity", "Error al dar de baja el modelo: ${error.message}")
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

    private fun loadModelos(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(ModeloApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarModelosFiltrados("Bearer $token", nombre, estado) }
            )

            result.onSuccess { modelos ->
                modelosList.clear()
                modelosList.addAll(modelos) // Agregar los modelos obtenidas
                adapter.updateList(modelosList) // Actualizar el RecyclerView con los modelos
            }.onFailure { error ->
                Snackbar.make(
                    findViewById(R.id.recyclerView),
                    "Error al cargar los modelos: ${error.message}",
                    Snackbar.LENGTH_LONG
                ).show()
                Log.e("ModelosActivity", "Error al cargar los modelos: ${error.message}")
            }
        }
    }

    private fun showDetalleModeloFragment(modelo: Modelo) {
        val fragment = DetalleModeloFragment(modelo) { updatedModelo ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(ModeloApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.editarModelo("Bearer $token", updatedModelo) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Modelo actualizada correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadModelos(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar la marca: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("ModelosActivity", "Error al actualizar el modelo: ${error.message}\nPayload: ${updatedModelo}")
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

        fragment.show(supportFragmentManager, "DetalleModeloFragment")
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

        filterMarcas(nombre, estado) // Aplica los filtros localmente
    }

    // Función para filtrar la lista de marcas (actualizada)
    private fun filterMarcas(nombre: String?, estado: Estado) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""
        val statusFilter = estado

        // Filtrar la lista de marcas
        filteredList = modelosList.filter { modelo ->
            val matchesName = modelo.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = modelo.estado == statusFilter

            matchesName && matchesStatus
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}