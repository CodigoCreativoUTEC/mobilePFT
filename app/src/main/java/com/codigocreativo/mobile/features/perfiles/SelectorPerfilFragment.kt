package com.codigocreativo.mobile.features.perfiles

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
import android.widget.AdapterView


class SelectorPerfilFragment : Fragment() {

    private lateinit var spinnerPerfil: Spinner
    private val dataRepository = DataRepository()
    private var perfiles: List<Perfil> = emptyList()
    private var pendingPerfilSelection: String? = null

    // LiveData to observe the loading state and selected profile
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    private val _selectedPerfil = MutableLiveData<Perfil?>()
    val selectedPerfil: LiveData<Perfil?> get() = _selectedPerfil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_perfil, container, false)
        spinnerPerfil = view.findViewById(R.id.spinnerPerfil)
        cargarPerfiles()
        setupSpinnerListener() // Configurar el listener para el spinner
        return view
    }

    private fun cargarPerfiles() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val perfilApiService = retrofit.create(PerfilApiService::class.java)

            viewLifecycleOwner.lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    perfilApiService.listarPerfiles("Bearer $token")
                }

                result.onSuccess { perfilesList ->
                    perfiles = perfilesList
                    val nombresPerfiles = perfiles.map { it.nombrePerfil }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPerfiles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerPerfil.adapter = adapter

                    // Set pending selection if there was one
                    pendingPerfilSelection?.let {
                        setSelectedPerfil(it)
                        pendingPerfilSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorPerfilFragment", "Error al cargar los perfiles", exception)
                }
            }
        }
    }

    fun getSelectedPerfil(): Perfil? {
        if (!::spinnerPerfil.isInitialized || spinnerPerfil.adapter == null) {
            Log.e("SelectorPerfilFragment", "spinnerPerfil is not initialized or adapter is not set")
            return null
        }
        val selectedPerfilName = spinnerPerfil.selectedItem.toString()
        return perfiles.find { it.nombrePerfil == selectedPerfilName }
    }

    fun setSelectedPerfil(nombrePerfil: String) {
        if (perfiles.isNotEmpty()) {
            val index = perfiles.indexOfFirst { it.nombrePerfil == nombrePerfil }
            if (index >= 0) {
                spinnerPerfil.setSelection(index)
            }
        } else {
            pendingPerfilSelection = nombrePerfil
        }
    }

    // Listener to update selected profile when user selects an item
    private fun setupSpinnerListener() {
        spinnerPerfil.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                _selectedPerfil.value = perfiles.getOrNull(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                _selectedPerfil.value = null
            }
        }
    }
}
