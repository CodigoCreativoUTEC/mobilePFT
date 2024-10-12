package com.codigocreativo.mobile.features.modelo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class ModelosActivity : AppCompatActivity() {

    private lateinit var adapter: ModeloAdapter
    private lateinit var recyclerView: RecyclerView
    var modelosList = mutableListOf<Modelo>() // Lista dinámica de modelos cargados desde el API
    private var filteredList = mutableListOf<Modelo>()
    private val dataRepository = DataRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modelos)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ModeloAdapter(filteredList, this)
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)
        Log.d("ModelosActivity", "Token: $token")
        if (token != null) {
            // Cargar los modelos desde el API
            loadModelos(token)
        } else {
            Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }

        // Configurar filtros
        setupFilters()
    }

    private fun loadModelos(token: String, nombre: String? = null, estado: String? = null) {
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(ModeloApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarModelos("Bearer $token", nombre, estado) }
            )

            result.onSuccess { modelos ->
                modelosList.clear()
                modelosList.addAll(modelos) // Agregar los modelos obtenidos
                adapter.updateList(modelosList) // Actualizar el RecyclerView con los modelos
            }.onFailure { error ->
                Log.e("ModelosActivity", "Error al cargar los modelos: ${error.message}")
            }
        }
    }

    private fun setupFilters() {
        val filterName: EditText = findViewById(R.id.filter_name)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        // Configuración del spinner de estado
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        // Listener para aplicar filtros
        val applyFilters = {
            val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)
            val nombre = filterName.text.toString().takeIf { it.isNotEmpty() }
            val estado = (filterStatus.selectedItem as Estado).name
            if (token != null) {
                loadModelos(token = token, nombre = nombre, estado = estado)
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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    // Método de filtro para modelos
    private fun filterModelos() {
        val nameFilter = findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de modelos
        filteredList = modelosList.filter { modelo ->
            modelo.nombre.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    (statusFilter == Estado.ACTIVO || statusFilter == Estado.INACTIVO || modelo.estado == statusFilter)
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }
}
