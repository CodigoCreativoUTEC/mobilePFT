package com.codigocreativo.mobile.features.paises

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class PaisesActivity : AppCompatActivity() {

    companion object {
        private const val TOKEN_NOT_FOUND_MESSAGE = "Token no encontrado, por favor inicia sesión"
    }

    private lateinit var adapter: PaisAdapter
    private lateinit var recyclerView: RecyclerView
    private var paisesList = mutableListOf<Pais>() // Lista dinámica de países cargados desde el API
    private var filteredList = mutableListOf<Pais>()
    private val dataRepository = DataRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paises)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PaisAdapter(filteredList, this) { pais ->
            showDetallePaisFragment(pais)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los países desde el API
            loadPaises(token)
        } else {
            Snackbar.make(
                findViewById(R.id.recyclerView),
                TOKEN_NOT_FOUND_MESSAGE,
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar País
        val fabAddPais: FloatingActionButton = findViewById(R.id.fab_add_pais)
        fabAddPais.setOnClickListener {
            showIngresarPaisFragment()
        }

        // Configurar ItemTouchHelper para swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                
                // Verificar que la posición sea válida y la lista no esté vacía
                if (position < 0 || position >= filteredList.size) {
                    Log.e("PaisesActivity", "Posición inválida: $position, tamaño de lista: ${filteredList.size}")
                    adapter.notifyItemChanged(position)
                    return
                }
                
                val pais = filteredList[position]

                AlertDialog.Builder(this@PaisesActivity)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Desea dar de baja el país '${pais.nombre}'?")
                    .setPositiveButton("Si") { _, _ ->
                        val token = SessionManager.getToken(this@PaisesActivity)
                        if (token != null) {
                            val retrofit = RetrofitClient.getClient(token)
                            val apiService = retrofit.create(PaisApiService::class.java)

                            lifecycleScope.launch {
                                val result = dataRepository.guardarDatos(
                                    token = token,
                                    apiCall = { apiService.inactivarPais("Bearer $token", pais.id!!) }
                                )

                                result.onSuccess {
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "País dado de baja correctamente",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    loadPaises(token)
                                }.onFailure { error ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error al dar de baja el país: ${error.message}",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    Log.e("PaisesActivity", "Error al dar de baja el país: ${error.message}")
                                    // Restaurar el elemento si falla la eliminación
                                    adapter.notifyItemChanged(position)
                                }
                            }
                        } else {
                            Snackbar.make(
                                findViewById(R.id.recyclerView),
                                TOKEN_NOT_FOUND_MESSAGE,
                                Snackbar.LENGTH_LONG
                            ).show()
                            // Restaurar el elemento si no hay token
                            adapter.notifyItemChanged(position)
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                        adapter.notifyItemChanged(position)
                    }
                    .create()
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        val searchView: SearchView = findViewById(R.id.search_view)
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setImageResource(R.drawable.ic_close)
    }

    private fun loadPaises(token: String) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(PaisApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarPaises("Bearer $token") }
            )

            result.onSuccess { paises ->
                paisesList.clear()
                paisesList.addAll(paises) // Agregar los países obtenidos
                adapter.updateList(paisesList) // Actualizar el RecyclerView con los países
            }.onFailure { error ->
                Snackbar.make(
                    findViewById(R.id.recyclerView),
                    "Error al cargar los países: ${error.message}",
                    Snackbar.LENGTH_LONG
                ).show()
                Log.e("PaisesActivity", "Error al cargar los países: ${error.message}")
            }
        }
    }

    private fun showDetallePaisFragment(pais: Pais) {
        val fragment = DetallePaisFragment(pais) { updatedPais ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(PaisApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.modificarPais("Bearer $token", updatedPais) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "País actualizado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadPaises(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar el país: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("PaisesActivity", "Error al actualizar el país: ${error.message}\nPayload: ${updatedPais}")
                    }
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.recyclerView),
                    TOKEN_NOT_FOUND_MESSAGE,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        fragment.show(supportFragmentManager, "DetallePaisFragment")
    }

    private fun showIngresarPaisFragment() {
        val fragment = IngresarPaisFragment { nuevoPais ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(PaisApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.crearPais("Bearer $token", nuevoPais) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "País creado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadPaises(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al crear el país: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("PaisesActivity", "Error al crear el país: ${error.message}\nPayload: ${nuevoPais}")
                    }
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.recyclerView),
                    TOKEN_NOT_FOUND_MESSAGE,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        fragment.show(supportFragmentManager, "IngresarPaisFragment")
    }

    private fun setupFilters() {
        val searchView: SearchView = findViewById(R.id.search_view)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        // Configurar el spinner de estado
        val estadoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = estadoAdapter

        // Configurar listeners
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No hacer nada especial al enviar
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })

        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se requiere acción cuando no hay selección en el spinner
            }
        }
    }

    private fun applyFilters() {
        val searchView: SearchView = findViewById(R.id.search_view)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        val nombre = searchView.query.toString().takeIf { it.isNotEmpty() }
        val estado = filterStatus.selectedItem as Estado // Ya es de tipo Estado

        filterPaises(nombre, estado) // Aplica los filtros localmente
    }

    // Función para filtrar la lista de países (actualizada)
    private fun filterPaises(nombre: String?, estado: Estado) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""
        val statusFilter = estado

        // Filtrar la lista de países
        filteredList = paisesList.filter { pais ->
            val matchesName = pais.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = pais.estado == statusFilter

            matchesName && matchesStatus
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
} 