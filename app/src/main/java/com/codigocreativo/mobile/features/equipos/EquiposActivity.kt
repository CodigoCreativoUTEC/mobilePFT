package com.codigocreativo.mobile.features.equipos

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
import com.codigocreativo.mobile.main.DashboardActivity
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
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
        findViewById<Button>(R.id.btn_ingresar).setOnClickListener {
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
        // Action listener de Volver al Menú
        findViewById<Button>(R.id.btn_volver_menu).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
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

                    // Show confirmation dialog
                    AlertDialog.Builder(this@EquiposActivity).apply {
                        setTitle("Confirmar baja")
                        setMessage("¿Estás seguro que deseas dar de baja al equipo ${equipo.nombre}?")
                        setPositiveButton("Si") { _, _ ->
                            if (token != null) {
                                val retrofit = RetrofitClient.getClient(token)
                                val apiService = retrofit.create(EquiposApiService::class.java)

                                lifecycleScope.launch {
                                    val result = dataRepository.guardarDatos(
                                        token = token,
                                        apiCall = {
                                            apiService.eliminar(
                                                "Bearer $token",
                                                equipo.id!!
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
                loadEquipos(token = token, nombre = nombre, estado = estado)
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

    // Método de filtro para equipos
    private fun filterEquipos() {
        val nameFilter =
            findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de equipos
        filteredList = equiposList.filter { equipos ->
            equipos.nombre.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    (statusFilter == Estado.ACTIVO || statusFilter == Estado.INACTIVO || equipos.estado == statusFilter)
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}