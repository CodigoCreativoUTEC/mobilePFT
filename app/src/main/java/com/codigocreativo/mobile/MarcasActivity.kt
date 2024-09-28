package com.codigocreativo.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MarcasActivity : AppCompatActivity() {

    private lateinit var adapter: MarcaAdapter
    private lateinit var recyclerView: RecyclerView
    var marcasList = listOf(
        Marca(1, "Toyota", Estado.ACTIVO),
        Marca(2, "Ford", Estado.INACTIVO),
        Marca(3, "BMW", Estado.ACTIVO),
        Marca(4, "Mercedes", Estado.INACTIVO),
        Marca(5, "Audi", Estado.ACTIVO)
    )

    private var filteredList = marcasList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcas)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MarcaAdapter(filteredList, this)
        recyclerView.adapter = adapter

        // Configurar filtros
        setupFilters()
        setupReturnButton()
    }

    private fun setupFilters() {
        val filterName: EditText = findViewById(R.id.filter_name)
        val filterStatus: Spinner = findViewById(R.id.filter_status)
        val btnClearFilter: Button = findViewById(R.id.btn_clear_filter)

        // Configuración del adaptador del Spinner de estado
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterMarcas()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Filtro por nombre
        filterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMarcas()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Funcionalidad del botón "Limpiar Filtro"
        btnClearFilter.setOnClickListener {
            filterName.text.clear()  // Limpiar el campo de texto
            filterMarcas()  // Refrescar la lista con todas las marcas
        }
    }


    private fun setupReturnButton() {
        val btnVolverMenu: Button = findViewById(R.id.btn_volver_menu)

        // Acción de volver al menú principal
        btnVolverMenu.setOnClickListener {
            finish()  // Termina la actividad actual y vuelve a la anterior
        }
    }


    private fun filterMarcas() {
        val nameFilter = findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        // Filtrar la lista de marcas por nombre y estado
        filteredList = marcasList.filter { marca ->
            // Comparar el estado seleccionado en el spinner con el estado de la marca
            val nameMatches = marca.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val statusMatches = marca.estado == statusFilter
            nameMatches && statusMatches
        }.toMutableList()

        // Actualizar el RecyclerView con la lista filtrada
        adapter.updateList(filteredList)
    }

}


