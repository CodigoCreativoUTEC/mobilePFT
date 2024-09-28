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
import com.codigocreativo.mobile.objetos.Estado
import java.util.Locale

class ModelosActivity : AppCompatActivity() {

    private lateinit var adapter: ModeloAdapter
    private lateinit var recyclerView: RecyclerView
    var modelosList = listOf(
        Modelo(1, "Corolla", Estado.ACTIVO),
        Modelo(2, "Mustang", Estado.INACTIVO),
        Modelo(3, "X5", Estado.ACTIVO),
        Modelo(4, "C-Class", Estado.INACTIVO),
        Modelo(5, "A4", Estado.ACTIVO)
    )

    private var filteredList = modelosList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modelos)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ModeloAdapter(filteredList, this)
        recyclerView.adapter = adapter

        setupFilters()
        setupReturnButton()
    }

    private fun setupFilters() {
        val filterName: EditText = findViewById(R.id.filter_name)
        val filterStatus: Spinner = findViewById(R.id.filter_status)
        val btnClearFilter: Button = findViewById(R.id.btn_clear_filter)

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterStatus.adapter = statusAdapter

        filterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterModelos()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        filterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterModelos()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearFilter.setOnClickListener {
            filterName.text.clear()
            filterModelos()
        }
    }

    private fun setupReturnButton() {
        val btnVolverMenu: Button = findViewById(R.id.btn_volver_menu)
        btnVolverMenu.setOnClickListener {
            finish()
        }
    }

    private fun filterModelos() {
        val nameFilter = findViewById<EditText>(R.id.filter_name).text.toString().lowercase(Locale.getDefault())
        val statusFilter = findViewById<Spinner>(R.id.filter_status).selectedItem as Estado

        filteredList = modelosList.filter { modelo ->
            val nameMatches = modelo.nombre.lowercase(Locale.getDefault()).contains(nameFilter)
            val statusMatches = modelo.estado == statusFilter
            nameMatches && statusMatches
        }.toMutableList()

        adapter.updateList(filteredList)
    }
}
