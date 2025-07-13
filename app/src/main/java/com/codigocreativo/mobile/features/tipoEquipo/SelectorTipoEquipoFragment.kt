package com.codigocreativo.mobile.features.tipoEquipo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.SessionManager
import kotlinx.coroutines.launch

class SelectorTipoEquipoFragment : Fragment() {

    private lateinit var spinnerTipoEquipo: Spinner
    private val dataRepository = DataRepository()
    private var tiposEquipos: List<TipoEquipo> = emptyList()
    private var pendingSelection: String? = null

    // LiveData to observe the loading state
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_tipo_equipo, container, false)
        spinnerTipoEquipo = view.findViewById(R.id.spinnerTipoEquipo)
        cargarTipoEquipos()
        return view
    }

    private fun cargarTipoEquipos() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val paisApiService = retrofit.create(TipoEquipoApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    paisApiService.listarTipoEquipos("Bearer $token")
                }

                result.onSuccess { tiposEquiposList ->
                    tiposEquipos = tiposEquiposList
                    val nombresTipoEquipoes = tiposEquipos.map { it.nombreTipo }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        nombresTipoEquipoes
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTipoEquipo.adapter = adapter

                    // If there was a pending selection, set it now
                    pendingSelection?.let {
                        setSelectedTipo(it)
                        pendingSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorTipoEquipoFragment", "Error al cargar los países", exception)
                }
            }
        }
    }

    fun getSelectedTipo(): TipoEquipo? {
        val selectedCountryName = spinnerTipoEquipo.selectedItem.toString()
        return tiposEquipos.find { it.nombreTipo == selectedCountryName }
    }

    fun setSelectedTipo(nombreTipoEquipo: String) {
        if (tiposEquipos.isNotEmpty()) {
            val index = tiposEquipos.indexOfFirst { it.nombreTipo == nombreTipoEquipo }
            if (index >= 0) {
                spinnerTipoEquipo.setSelection(index)
            }
        } else {
            // If paises is not yet initialized, store the selection for later
            pendingSelection = nombreTipoEquipo
        }
    }
}