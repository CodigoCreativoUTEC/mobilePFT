package com.codigocreativo.mobile.features.marca

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
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

        val btnAgregar = findViewById<ImageView>(R.id.btn_agregar)
        btnAgregar.setOnClickListener{
            showAddMarcaDialog()
        }

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

    // Método para mostrar el diálogo para agregar una nueva marca
    private fun showAddMarcaDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_marca, null)
        val nombreEditText: EditText = dialogView.findViewById(R.id.editTextNombre)

        AlertDialog.Builder(this)
            .setTitle("Agregar Marca")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = nombreEditText.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    addMarca(nombre)
                } else {
                    Snackbar.make(findViewById(R.id.main), "El nombre no puede estar vacío", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Método para agregar una nueva marca
    private fun addMarca(nombre: String) {
        // Crear una nueva marca con ID temporal (-1)
        val nuevaMarca = Marca(id = -1, nombre = nombre, estado = Estado.ACTIVO)

        // Añadir temporalmente la marca a las listas locales
        marcasList.add(nuevaMarca)
        filteredList.add(nuevaMarca)
        adapter.updateList(filteredList)

        // Obtener el token
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)

        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(MarcaApiService::class.java)

            // Llamar a la API dentro de una corrutina
            lifecycleScope.launch {
                try {
                    // Llamada a la API para crear la nueva marca
                    val response = apiService.crearMarca("Bearer $token", nuevaMarca)

                    if (response.isSuccessful && response.body() != null) {
                        // Obtener la marca creada con el ID real del backend
                        val marcaCreada = response.body()!!

                        // Actualizar la marca en la lista local con el ID real
                        val index = marcasList.indexOf(nuevaMarca)
                        if (index != -1) {
                            marcasList[index] = marcaCreada
                            filteredList[filteredList.indexOf(nuevaMarca)] = marcaCreada
                            adapter.updateList(filteredList)
                        }

                        Snackbar.make(findViewById(R.id.main), "Marca agregada correctamente", Snackbar.LENGTH_SHORT).show()
                    } else {
                        // Manejo de errores si la respuesta no es exitosa
                        Log.e("MarcasActivity", "Error en la respuesta de la API: ${response.errorBody()?.string()}")
                        Snackbar.make(findViewById(R.id.main), "Error al agregar la marca", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Manejo de excepciones durante la llamada a la API
                    Log.e("MarcasActivity", "Error al agregar la marca: ${e.message}")
                    Snackbar.make(findViewById(R.id.main), "Error al agregar la marca", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }


    // Método para mostrar el diálogo para editar una marca existente
    @SuppressLint("MissingInflatedId")
    fun showEditMarcaDialog(marca: Marca) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_marca, null)
        val nombreEditText: EditText = dialogView.findViewById(R.id.editTextNombre)
        nombreEditText.setText(marca.nombre)

        AlertDialog.Builder(this)
            .setTitle("Editar Marca")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = nombreEditText.text.toString().trim()
                if (nuevoNombre.isNotEmpty()) {
                    editMarca(marca, nuevoNombre)
                } else {
                    Snackbar.make(findViewById(R.id.main), "El nombre no puede estar vacío", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Método para editar una marca
    private fun editMarca(marca: Marca, nuevoNombre: String) {
        val index = marcasList.indexOf(marca)
        if (index != -1) {
            // Actualizar el nombre en las listas locales
            marcasList[index].nombre = nuevoNombre
            filteredList[index].nombre = nuevoNombre
            adapter.updateList(filteredList)

            // Aquí puedes hacer una petición a la API para actualizar la marca en la base de datos
            val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("jwt_token", null)

            if (token != null) {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(MarcaApiService::class.java)

                // Llamar a la API dentro de una corrutina
                lifecycleScope.launch {
                    try {
                        // Llamada a la API para editar la marca
                        val response = apiService.editarMarca("Bearer $token", marca.id, marca.copy(nombre = nuevoNombre))

                        if (response.isSuccessful) {
                            Snackbar.make(findViewById(R.id.main), "Marca editada correctamente", Snackbar.LENGTH_SHORT).show()
                        } else {
                            // Manejo de errores en caso de respuesta no exitosa
                            Log.e("MarcasActivity", "Error en la respuesta de la API: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        // Manejo de cualquier excepción durante la llamada a la API
                        Log.e("MarcasActivity", "Error al editar la marca: ${e.message}")
                    }
                }
            } else {
                Snackbar.make(findViewById(R.id.main), "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
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
