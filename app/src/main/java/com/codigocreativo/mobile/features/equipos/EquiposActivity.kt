package com.codigocreativo.mobile.features.equipos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EquiposActivity : AppCompatActivity() {

    companion object {
        private const val TOKEN_NOT_FOUND_MSG = "Token no encontrado, por favor inicia sesión"
    }

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
                findViewById(R.id.root_layout),
                TOKEN_NOT_FOUND_MSG,
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Configurar filtros
        setupFilters()

        // Action listener de Ingresar Equipo
        findViewById<ImageView>(R.id.image_add).setOnClickListener {
            val bottomSheetFragment = IngresarEquipoFragment { equipoRequest ->
                if (token != null) {
                    val retrofit = RetrofitClient.getClient(token)
                    val apiService = retrofit.create(EquiposApiService::class.java)

                    lifecycleScope.launch {
                        val result = dataRepository.guardarDatos(
                            token = token,
                            apiCall = {
                                apiService.crear("Bearer $token", equipoRequest)
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
                                "Error al ingresar el equipo: ${error.message}\nPayload: ${equipoRequest}"
                            )
                        }
                    }
                } else {
                    Snackbar.make(
                        findViewById(R.id.root_layout),
                        TOKEN_NOT_FOUND_MSG,
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

                    val etRazonBaja = dialogView.findViewById<TextInputEditText>(R.id.etRazonBaja)
                    val etFechaBaja = dialogView.findViewById<TextInputEditText>(R.id.etFechaBaja)
                    val etComentarios = dialogView.findViewById<TextInputEditText>(R.id.etComentarios)

                    // Function to show a toast message
                    fun showToast(message: String) {
                        Toast.makeText(this@EquiposActivity, message, Toast.LENGTH_SHORT).show()
                    }

                    // Set default fecha baja to current date
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    etFechaBaja.setText(currentDate)

                    // Create the alert dialog
                    val dialog = AlertDialog.Builder(this@EquiposActivity).apply {
                        setView(dialogView)
                        setCancelable(false)
                        create()
                    }.create()

                    // Set up button click listeners
                    val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
                    val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.btnConfirmar)

                    btnCancelar.setOnClickListener {
                        dialog.dismiss()
                        adapter.notifyItemChanged(position)
                    }

                    btnConfirmar.setOnClickListener {
                        val razonBaja = etRazonBaja.text.toString()
                        val fechaBaja = etFechaBaja.text.toString()
                        val comentarios = etComentarios.text.toString()

                        // Validate that razón de baja is not empty
                        if (razonBaja.isEmpty()) {
                            showToast("La razón de la baja es obligatoria")
                            return@setOnClickListener
                        }

                        //obtener el objeto usuario que esta logueado
                        val usuario = SessionManager.getLoggedUser(this@EquiposActivity)

                        // Prepare the request object
                        val bajaEquipoRequest = usuario?.let {
                            BajaEquipoRequest(
                                razon = razonBaja,
                                fecha = fechaBaja,
                                idUsuario = it,
                                idEquipo = equipo,
                                estado = Estado.ACTIVO,
                                comentarios = comentarios
                            )
                        }

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
                                            bajaEquipoRequest  // Pass the object as the body
                                        )
                                    }
                                )

                                result.onSuccess {
                                    dialog.dismiss()
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
                                findViewById(R.id.root_layout),
                                TOKEN_NOT_FOUND_MSG,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    dialog.show()
                }
            })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        val searchView: SearchView = findViewById(R.id.search_view)
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setImageResource(R.drawable.ic_close)
    }


    private fun loadEquipos(token: String) {
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
                    val equipoRequest = convertirAEquipoRequest(updatedEquipo)
                    val result = dataRepository.guardarDatos(
                        token = token,
                        apiCall = { apiService.actualizar("Bearer $token", equipoRequest) }
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
                    findViewById(R.id.root_layout),
                    TOKEN_NOT_FOUND_MSG,
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

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se requiere acción cuando no hay selección en el spinner
            }
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

    // Función para convertir Equipo a EquipoRequest (objetos completos)
    private fun convertirAEquipoRequest(equipo: Equipo): EquipoRequest {
        return EquipoRequest(
            id = equipo.id,
            nombre = equipo.nombre,
            idModelo = equipo.idModelo,
            estado = equipo.estado,
            equiposUbicaciones = equipo.equiposUbicaciones,
            fechaAdquisicion = equipo.fechaAdquisicion,
            garantia = equipo.garantia,
            idInterno = equipo.idInterno,
            idPais = equipo.idPais,
            idProveedor = equipo.idProveedor,
            idTipo = equipo.idTipo,
            idUbicacion = equipo.idUbicacion,
            imagen = equipo.imagen,
            nroSerie = equipo.nroSerie,
            descripcion = equipo.descripcion
        )
    }
}