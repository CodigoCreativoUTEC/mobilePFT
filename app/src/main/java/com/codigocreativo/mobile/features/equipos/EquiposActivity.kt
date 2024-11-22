package com.codigocreativo.mobile.features.equipos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.equipos.bajaEquipo.BajaEquipoRequest
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EquiposActivity : AppCompatActivity() {

    private lateinit var adapter: EquipoAdapter
    private lateinit var recyclerView: RecyclerView
    private var equiposList =
        mutableListOf<Equipo>() // Lista dinámica de equipos cargados desde el API
    private var filteredList = mutableListOf<Equipo>()
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("EquiposActivity", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipos)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EquipoAdapter(filteredList, this) { equipo ->
            showDetalleEquipoFragment(equipo)
        }
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = SessionManager.getToken(this)
        if (token != null) {
            // Cargar los equipos desde el API
            loadEquipos(token)
        } else {
            Snackbar.make(
                findViewById(R.id.main),
                "Token no encontrado, por favor inicia sesión",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar Equipo
        findViewById<ImageView>(R.id.image_add).setOnClickListener {
            val bottomSheetFragment = IngresarEquipoFragment { equipo ->
                if (token != null) {
                    val retrofit = RetrofitClient.getClient(token)
                    val apiService = retrofit.create(EquiposApiService::class.java)

                    lifecycleScope.launch {
                        val result = dataRepository.guardarDatos(
                            token = token,
                            apiCall = {
                                apiService.crear("Bearer $token", equipo)
                            })

                        result.onSuccess {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Equipo ingresado correctamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadEquipos(token)
                        }.onFailure { error ->
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Error al ingresar el equipo: ${error.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e(
                                "EquiposActivity",
                                "Error al ingresar el equipo: ${error.message}\nPayload: ${equipo}"
                            )
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

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val equipo = adapter.equipoList[position]

                    // Show confirmation dialog for reason and comments
                    val inflater = LayoutInflater.from(this@EquiposActivity)
                    val dialogView = inflater.inflate(R.layout.dialog_baja_equipo, null)

                    val etRazonBaja = dialogView.findViewById<EditText>(R.id.etRazonBaja)
                    val etFechaBaja = dialogView.findViewById<EditText>(R.id.etFechaBaja)
                    val etComentarios = dialogView.findViewById<EditText>(R.id.etComentarios)

                    // Function to show a toast message
                    fun showToast(message: String) {
                        Toast.makeText(this@EquiposActivity, message, Toast.LENGTH_SHORT).show()
                    }

                    // Set default fecha baja to current date
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    etFechaBaja.setText(currentDate)

                    // Create the alert dialog
                    AlertDialog.Builder(this@EquiposActivity).apply {
                        setTitle("Confirmar baja de equipo")
                        setMessage("Por favor, ingresa los detalles para confirmar la baja del equipo ${equipo.nombre}")
                        setView(dialogView)
                        setPositiveButton("Confirmar") { _, _ ->
                            val razonBaja = etRazonBaja.text.toString()
                            val fechaBaja = etFechaBaja.text.toString()
                            val comentarios = etComentarios.text.toString()

                            // Validate that razón de baja is not empty
                            if (razonBaja.isEmpty()) {
                                showToast("La razón de la baja es obligatoria")
                                return@setPositiveButton
                            }

                            // Prepare the request object
                            val bajaEquipoRequest = BajaEquipoRequest(
                                razonBaja = razonBaja,
                                fechaBaja = fechaBaja,
                                comentarios = comentarios
                            )

                            // Realizar la baja lógica del equipo
                            if (token != null) {
                                val retrofit = RetrofitClient.getClient(token)
                                val apiService = retrofit.create(EquiposApiService::class.java)

                                lifecycleScope.launch {
                                    val result = dataRepository.guardarDatos(
                                        token = token,
                                        apiCall = {
                                            apiService.eliminar(
                                                "Bearer $token",
                                                equipo.id!!,
                                                bajaEquipoRequest  // Pass the object as the body
                                            )
                                        }
                                    )

                                    result.onSuccess {
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            "Equipo dado de baja correctamente",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        loadEquipos(token)
                                    }.onFailure { error ->
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            "Error al dar de baja al equipo: ${error.message}",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        Log.e(
                                            "EquiposActivity",
                                            "Error al dar de baja al equipo: ${error.message}"
                                        )
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
                        setNegativeButton("Cancelar") { dialog, _ ->
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


    private fun loadEquipos(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(EquiposApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listar("Bearer $token") }
            )

            result.onSuccess { equipos ->
                equiposList.clear()
                equiposList.addAll(equipos) // Agregar los equipos obtenidos
                adapter.updateList(equiposList) // Actualizar el RecyclerView con los equipos
            }.onFailure { error ->
                Log.e("EquiposActivity", "Error al cargar los equipos: ${error.message}")
            }
        }
    }

    private fun showDetalleEquipoFragment(equipo: Equipo) {
        val fragment = DetalleEquipoFragment(equipo) { updatedEquipo ->
            val token = SessionManager.getToken(this)
            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(EquiposApiService::class.java)

                lifecycleScope.launch {
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.actualizar("Bearer $token", updatedEquipo) }
                    )

                    result.onSuccess {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Equipo actualizado correctamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadEquipos(token)
                    }.onFailure { error ->
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error al actualizar el equipo: ${error.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e(
                            "EquiposActivity",
                            "Error al actualizar el equipo: ${error.message}\nPayload: ${updatedEquipo}"
                        )
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

        fragment.show(supportFragmentManager, "DetalleEquipoFragment")
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

        filterEquipos(nombre, estado) // Aplica los filtros localmente
    }

    // Función para filtrar la lista de equipos (actualizada)
    private fun filterEquipos(nombre: String?, estado: Estado) {
        val nameFilter = nombre?.lowercase(Locale.getDefault()) ?: ""
        val statusFilter = estado

        // Filtrar la lista de marcas
        filteredList = equiposList.filter { modelo ->
            val matchesName = modelo.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val matchesStatus = modelo.estado == statusFilter

            matchesName && matchesStatus
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}