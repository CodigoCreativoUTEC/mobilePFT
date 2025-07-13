package com.codigocreativo.mobile.features.ubicacion

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

class SelectorUbicacionFragment : Fragment() {

    private lateinit var spinnerUbicacion: Spinner
    private val dataRepository = DataRepository()
    private var ubicaciones: List<Ubicacion> = emptyList()
    private var pendingSelection: String? = null

    // LiveData to observe the loading state
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_ubicacion, container, false)
        spinnerUbicacion = view.findViewById(R.id.spinnerUbicacion)
        cargarUbicaciones()
        return view
    }

    private fun cargarUbicaciones() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val ubicacionApiService = retrofit.create(UbicacionesApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    ubicacionApiService.listar("Bearer $token")
                }

                result.onSuccess { ubicacionesList ->
                    ubicaciones = ubicacionesList
                    val nombresUbicaciones = ubicaciones.map { it.nombre }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        nombresUbicaciones
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerUbicacion.adapter = adapter

                    // If there was a pending selection, set it now
                    pendingSelection?.let {
                        setSelectedUbicacion(it)
                        pendingSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorUbicacionFragment", "Error al cargar los países", exception)
                }
            }
        }
    }

    fun getSelectedUbicacion(): Ubicacion? {
        val selectedCountryName = spinnerUbicacion.selectedItem.toString()
        return ubicaciones.find { it.nombre == selectedCountryName }
    }

    fun setSelectedUbicacion(nombreUbicacion: String) {
        if (ubicaciones.isNotEmpty()) {
            val index = ubicaciones.indexOfFirst { it.nombre == nombreUbicacion }
            if (index >= 0) {
                spinnerUbicacion.setSelection(index)
            }
        } else {
            // If ubicaciones is not yet initialized, store the selection for later
            pendingSelection = nombreUbicacion
        }
    }
}