package com.codigocreativo.mobile.features.paises

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

class SelectorPaisFragment : Fragment() {

    private lateinit var spinnerPais: Spinner
    private val dataRepository = DataRepository()
    private var paises: List<Pais> = emptyList()
    private var pendingCountrySelection: String? = null

    // LiveData to observe the loading state
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_pais, container, false)
        spinnerPais = view.findViewById(R.id.spinnerPais)
        cargarPaises()
        return view
    }

    private fun cargarPaises() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val paisApiService = retrofit.create(PaisApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    paisApiService.listarPaises("Bearer $token")
                }

                result.onSuccess { paisesList ->
                    paises = paisesList
                    val nombresPaises = paises.map { it.nombre }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPaises)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerPais.adapter = adapter

                    // If there was a pending country selection, set it now
                    pendingCountrySelection?.let {
                        setSelectedCountry(it)
                        pendingCountrySelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorPaisFragment", "Error al cargar los países", exception)
                }
            }
        }
    }

    fun getSelectedCountry(): Pais? {
        val selectedCountryName = spinnerPais.selectedItem.toString()
        return paises.find { it.nombre == selectedCountryName }
    }

    fun setSelectedCountry(nombrePais: String) {
        if (paises.isNotEmpty()) {
            val index = paises.indexOfFirst { it.nombre == nombrePais }
            if (index >= 0) {
                spinnerPais.setSelection(index)
            }
        } else {
            // If paises is not yet initialized, store the selection for later
            pendingCountrySelection = nombrePais
        }
    }
}