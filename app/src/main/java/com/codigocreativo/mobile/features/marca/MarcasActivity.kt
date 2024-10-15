package com.codigocreativo.mobile.features.marca

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

class MarcasActivity : AppCompatActivity() {

    private lateinit var adapter: MarcaAdapter
    private lateinit var recyclerView: RecyclerView
    var marcasList = mutableListOf<Marca>() // Lista dinámica de marcas cargadas desde el API
    private var filteredList = mutableListOf<Marca>()
    private val dataRepository = DataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcas)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MarcaAdapter(filteredList, this)
        recyclerView.adapter = adapter

        // Obtener el token JWT almacenado
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)
        if (token != null) {
            // Cargar las marcas desde el API
            loadMarcas(token)
        } else {
            Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }

        // Configurar filtros
        setupFilters()
    }

    private fun loadMarcas(token: String) {
        // Hacer la petición para obtener las marcas desde el API
        val retrofit = RetrofitClient.getClient(token)
        val apiService = retrofit.create(MarcaApiService::class.java)

        lifecycleScope.launch {
            val result = dataRepository.obtenerDatos(
                token = token,
                apiCall = { apiService.listarMarcas("Bearer $token") }
            )

            result.onSuccess { marcas ->
                marcasList.clear()
                marcasList.addAll(marcas) // Agregar las marcas obtenidas
                filteredList.clear()
                filteredList.addAll(marcasList)
                adapter.updateList(filteredList) // Actualizar el RecyclerView con las marcas
            }.onFailure { error ->
                Log.e("MarcasActivity", "Error al cargar las marcas: ${error.message}")
            }
        }
    }

    // Configurar filtros (sin cambios)
    private fun setupFilters() {
        val filterName: EditText = findViewById(R.id.filter_name)
        val filterStatus: Spinner = findViewById(R.id.filter_status)

        // Filtro por nombre
        filterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMarcas()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Configuración del spinner de estado
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterMarcas()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Método de filtro (sin cambios)
    private fun filterMarcas() {
        val nameFilter = findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de marcas
        filteredList = marcasList.filter { marca ->
            marca.nombre.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    (statusFilter == Estado.ACTIVO || statusFilter == Estado.INACTIVO || marca.estado == statusFilter)
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }

}
