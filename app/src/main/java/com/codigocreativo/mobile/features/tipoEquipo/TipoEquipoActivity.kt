package com.codigocreativo.mobile.features.tipoEquipo

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
import com.codigocreativo.mobile.features.modelo.DetalleModeloFragment
import com.codigocreativo.mobile.features.modelo.IngresarModeloFragment
import com.codigocreativo.mobile.features.modelo.Modelo
import com.codigocreativo.mobile.features.modelo.ModeloAdapter
import com.codigocreativo.mobile.features.modelo.ModeloApiService
import com.codigocreativo.mobile.main.DashboardActivity
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class TipoEquipoActivity : AppCompatActivity() {

    private lateinit var adapter: TipoEquipoAdapter
    private lateinit var recyclerView: RecyclerView
    private var tipoEquipoList = mutableListOf<TipoEquipo>() // Lista dinámica de marcas cargados desde el API
    private var filteredList = mutableListOf<TipoEquipo>()
    private val dataRepository = DataRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tipo_equipo)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TipoEquipoAdapter(filteredList, this) { tipoEquipo ->
            showDetalleTipoEquipoFragment(tipoEquipo)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los tipos de equipos desde el API
            loadTipoEquipos(token)
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
            val bottomSheetFragment = IngresarTipoEquipoFragment { tipoEquipo ->
                if (token != null) {
                    val retrofit = RetrofitClient.getClient(token)
                    val apiService = retrofit.create(TipoEquipoApiService::class.java)

                    lifecycleScope.launch {
                        val result = dataRepository.guardarDatos(
                            token = token,
                            apiCall = {
                                apiService.crearTipoEquipo("Bearer $token", tipoEquipo)
                            })

                        result.onSuccess {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Tipo de Equipo ingresado correctamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadTipoEquipos(token)
                        }.onFailure { error ->
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Error al ingresar el tipo de equipo: ${error.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e("TipoEquipoActivity", "Error al ingresar el tipode equipo: ${error.message}\nPayload: ${tipoEquipo}")
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
                val tipoEquipo = adapter.tipoEquipoList[position]

                // Show confirmation dialog
                androidx.appcompat.app.AlertDialog.Builder(this@TipoEquipoActivity).apply {
                    setTitle("Confirmar baja")
                    setMessage("¿Estás seguro que deseas dar de baja el tipo de equipo ${tipoEquipo.nombreTipo}?")
                    setPositiveButton("Si") { _, _ ->
                        val token = SessionManager.getToken(this@TipoEquipoActivity)
                        if (token != null) {
                            val retrofit = RetrofitClient.getClient(token)
                            val apiService = retrofit.create(TipoEquipoApiService::class.java)

                            lifecycleScope.launch {
                                val result = dataRepository.guardarDatos(
                                    token = token,
                                    apiCall = { apiService.eliminarTipoEquipo("Bearer $token",
                                        tipoEquipo.id!!
                                    ) }
                                )

                                result.onSuccess {
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Tipo de equipo dado de baja correctamente",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    loadTipoEquipos(token)
                                }.onFailure { error ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error al dar de baja el tipo de equipo: ${error.message}",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    Log.e("TipoEquipoActivity", "Error al dar de baja el tipo de equipo: ${error.message}")
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

    private fun loadTipoEquipos(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(TipoEquipoApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarTipoEquipos("Bearer $token", nombre, estado) }
            )

            result.onSuccess { tipoEquipo ->
                tipoEquipoList.clear()
                tipoEquipoList.addAll(tipoEquipo) // Agregar los tipo de equipos obtenidos
                adapter.updateList(tipoEquipoList) // Actualizar el RecyclerView con los tipos de equipos
            }.onFailure { error ->
                Snackbar.make(
                    findViewById(R.id.recyclerView),
                    "Error al cargar los tipos de equipos: ${error.message}",
                    Snackbar.LENGTH_LONG
                ).show()
                Log.e("TipoEquipoActivity", "Error al cargar los tipos de equipos: ${error.message}")
            }
        }
    }

    private fun showDetalleTipoEquipoFragment(tipoEquipo: TipoEquipo) {
        val fragment = DetalleTipoEquipoFragment(tipoEquipo) { updatedTipoEquipo ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(TipoEquipoApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.editarTipoEquipo("Bearer $token", updatedTipoEquipo) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Tipo de equipo actualizado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadTipoEquipos(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar el tipo de equipo: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("TipoEquipoActivity", "Error al actualizar el tipo de equipo: ${error.message}\nPayload: ${updatedTipoEquipo}")
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

        fragment.show(supportFragmentManager, "DetalleTipoEquipoFragment")
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

        filterTipoEquipos(nombre, estado) // Aplica los filtros localmente
    }

    // Función para filtrar la lista de marcas (actualizada)
    private fun filterTipoEquipos(nombre: String?, estado: Estado) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""
        val statusFilter = estado

        // Filtrar la lista de tipo de equipos
        filteredList = tipoEquipoList.filter { tipoEquipo ->
            val matchesName = tipoEquipo.nombreTipo.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = tipoEquipo.estado == statusFilter

            matchesName && matchesStatus
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}
