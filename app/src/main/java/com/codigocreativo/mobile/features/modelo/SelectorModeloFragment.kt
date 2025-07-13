package com.codigocreativo.mobile.features.modelo

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

class SelectorModeloFragment : Fragment() {

    private lateinit var spinnerModelo: Spinner
    private val dataRepository = DataRepository()
    private var modelos: List<Modelo> = emptyList()
    private var pendingModeloSelection: String? = null

    // LiveData to observe the loading state
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_modelo, container, false)
        spinnerModelo = view.findViewById(R.id.spinnerModelo)
        cargarModelos()
        return view
    }

    private fun cargarModelos() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val marcaApiService = retrofit.create(ModeloApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    marcaApiService.listarModelos("Bearer $token")
                }

                result.onSuccess { modelosList ->
                    modelos = modelosList
                    val nombresModelos = modelos.map { it.nombre }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresModelos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerModelo.adapter = adapter

                    // If there was a pending country selection, set it now
                    pendingModeloSelection?.let {
                        setSelectedModelo(it)
                        pendingModeloSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorModeloFragment", "Error al cargar los modelos", exception)
                }
            }
        }
    }

    fun getSelectedModelo(): Modelo? {
        val selectedModeloName = spinnerModelo.selectedItem.toString()
        return modelos.find { it.nombre == selectedModeloName }
    }

    fun setSelectedModelo(nombreModelo: String) {
        if (modelos.isNotEmpty()) {
            val index = modelos.indexOfFirst { it.nombre == nombreModelo }
            if (index >= 0) {
                spinnerModelo.setSelection(index)
            }
        } else {
            // If modelos is not yet initialized, store the selection for later
            pendingModeloSelection = nombreModelo
        }
    }
}