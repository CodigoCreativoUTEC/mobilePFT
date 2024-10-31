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
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
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
    private var tipoEquipoList = mutableListOf<TipoEquipo>() // Lista dinámica de tipo de equipos cargados desde el API
    private var filteredList = mutableListOf<TipoEquipo>()
    private val dataRepository = DataRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tipo_equipo)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TipoEquipoAdapter(filteredList, this) {tipoEquipo ->
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

        // Action listener de Ingresar Tipo de Equipo
        findViewById<Button>(R.id.btn_ingresar).setOnClickListener {
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
                                "Tipo de equipo ingresado correctamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadTipoEquipos(token)
                        }.onFailure { error ->
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Error al ingresar el tipo de equipo: ${error.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e("TipoEquipoActivity", "Error al ingresar el tipo de equipo: ${error.message}\nPayload: ${tipoEquipo}")
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
                    setMessage("¿Estás seguro que deseas dar de baja al tipo de equipo ${tipoEquipo.nombreTipo}?")
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
                                    Log.e("MarcasActivity", "Error al dar de baja el tipo de equipo: ${error.message}")
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

    private fun loadTipoEquipos(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(TipoEquipoApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarTipoEquipos("Bearer $token") }
            )

            result.onSuccess { tipoEquipo ->
                tipoEquipoList.clear()
                tipoEquipoList.addAll(tipoEquipo) // Agregar los tipos de equipos obtenidos
                adapter.updateList(tipoEquipoList) // Actualizar el RecyclerView con los tipos de equipos
            }.onFailure { error ->
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
                loadTipoEquipos(token = token, nombre = nombre, estado = estado)
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

    // Método de filtro para marcas
    private fun filterTipoEquipos() {
        val nameFilter =
            findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de marcas
        filteredList = tipoEquipoList.filter { tipoEquipos ->
            tipoEquipos.nombreTipo.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    (statusFilter == Estado.ACTIVO || statusFilter == Estado.INACTIVO || tipoEquipos.estado == statusFilter)
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}
