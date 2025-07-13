package com.codigocreativo.mobile.features.marca

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.paises.PaisApiService
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.SessionManager
import kotlinx.coroutines.launch

class SelectorMarcaFragment : Fragment() {

    private lateinit var spinnerMarca: Spinner
    private val dataRepository = DataRepository()
    private var marcas: List<Marca> = emptyList()
    private var pendingMarcaSelection: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_marca, container, false)
        spinnerMarca = view.findViewById(R.id.spinnerMarca)
        cargarMarcas()
        return view
    }

    private fun cargarMarcas() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val marcaApiService = retrofit.create(MarcaApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    marcaApiService.listarMarcas("Bearer $token")
                }

                result.onSuccess { marcasList ->
                    marcas = marcasList
                    val nombresMarcas = marcas.map { it.nombre }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresMarcas)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerMarca.adapter = adapter

                    // If there was a pending country selection, set it now
                    pendingMarcaSelection?.let {
                        setSelectedMarca(it)
                        pendingMarcaSelection = null
                    }
                }.onFailure { exception ->
                    Log.e("SelectorMarcaFragment", "Error al cargar las marcas", exception)
                }
            }
        }
    }

    fun getSelectedMarca(): Marca? {
        val selectedMarcaName = spinnerMarca.selectedItem.toString()
        return marcas.find { it.nombre == selectedMarcaName }
    }

    fun setSelectedMarca(nombreMarca: String) {
        if (marcas.isNotEmpty()) {
            val index = marcas.indexOfFirst { it.nombre == nombreMarca }
            if (index >= 0) {
                spinnerMarca.setSelection(index)
            }
        } else {
            // If marcas is not yet initialized, store the selection for later
            pendingMarcaSelection = nombreMarca
        }
    }
}