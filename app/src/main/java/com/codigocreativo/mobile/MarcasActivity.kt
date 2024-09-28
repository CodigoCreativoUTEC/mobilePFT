package com.codigocreativo.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MarcasActivity : AppCompatActivity() {

    // Simulación de datos que vendrían de la base de datos
    private val marcasList = listOf(
        Marca(1, "Toyota", Estado.ACTIVO),
        Marca(2, "Ford", Estado.INACTIVO),
        Marca(3, "BMW", Estado.ACTIVO),
        Marca(4, "Mercedes", Estado.INACTIVO),
        Marca(5, "Audi", Estado.ACTIVO)
    )

    // Lista filtrada
    private var filteredList = marcasList.toMutableList()

    // Adaptador para el RecyclerView
    private lateinit var adapter: MarcaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcas)

        // Configurar el RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MarcaAdapter(filteredList)
        recyclerView.adapter = adapter

        // Configurar el filtro de nombre
        val filterName: EditText = findViewById(R.id.filter_name)
        filterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMarcas()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Configurar el filtro de estado
        val filterStatus: Spinner = findViewById(R.id.filter_status)
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        // Aquí agregamos el `OnItemSelectedListener` correctamente
        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                filterMarcas()  // Llama a la función de filtro cada vez que se selecciona un nuevo estado
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }
    }

    // Función para filtrar las marcas
    private fun filterMarcas() {
        val nameFilter = findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        filteredList = marcasList.filter { marca ->
            marca.nombre.lowercase(Locale.getDefault()).contains(nameFilter) &&
                    marca.estado == statusFilter
        }.toMutableList()

        // Actualizar el RecyclerView
        adapter.updateList(filteredList)
    }
}
