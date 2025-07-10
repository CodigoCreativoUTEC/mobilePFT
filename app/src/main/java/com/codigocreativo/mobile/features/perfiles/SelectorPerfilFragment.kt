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
        Log.d("SelectorPerfilFragment", "onCreateView llamado")
        val view = inflater.inflate(R.layout.fragment_selector_perfil, container, false)
        spinnerPerfil = view.findViewById(R.id.spinnerPerfil)
        Log.d("SelectorPerfilFragment", "Spinner encontrado: ${spinnerPerfil != null}")
        cargarPerfiles()
        setupSpinnerListener() // Configurar el listener para el spinner
        return view
    }

    private fun cargarPerfiles() {
        Log.d("SelectorPerfilFragment", "Cargando perfiles...")
        
        // Para el registro, usamos perfiles hardcodeados ya que no tenemos token
        // En un entorno real, deberías tener un endpoint público para obtener perfiles
        perfiles = listOf(
            Perfil(id = 1, nombrePerfil = "Admin", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
            Perfil(id = 2, nombrePerfil = "Aux administrativo", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
            Perfil(id = 3, nombrePerfil = "Técnico", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
            Perfil(id = 4, nombrePerfil = "Supervisor", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO)
        )

        Log.d("SelectorPerfilFragment", "Perfiles cargados: ${perfiles.size} perfiles")

        val nombresPerfiles = perfiles.map { it.nombrePerfil }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPerfiles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPerfil.adapter = adapter

        Log.d("SelectorPerfilFragment", "Adapter configurado con ${nombresPerfiles.size} elementos")

        // Set pending selection if there was one
        pendingPerfilSelection?.let {
            setSelectedPerfil(it)
            pendingPerfilSelection = null
        }

        // Update the loading state
        _isDataLoaded.value = true
        Log.d("SelectorPerfilFragment", "Estado de carga actualizado: true")

        // Intentar cargar desde el servidor si hay token disponible
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            Log.d("SelectorPerfilFragment", "Token encontrado, intentando cargar desde servidor")
            cargarPerfilesDesdeServidor(token)
        } else {
            Log.d("SelectorPerfilFragment", "No hay token disponible, usando datos locales")
        }
    }

    private fun cargarPerfilesDesdeServidor(token: String) {
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
                Log.d("SelectorPerfilFragment", "Perfiles cargados desde servidor: ${perfiles.size} perfiles")
            }.onFailure { exception ->
                Log.e("SelectorPerfilFragment", "Error al cargar los perfiles desde servidor, usando datos locales", exception)
                // Si falla, mantenemos los datos hardcodeados
            }
        }
    }

    fun getSelectedPerfil(): Perfil? {
        if (!::spinnerPerfil.isInitialized || spinnerPerfil.adapter == null) {
            Log.e("SelectorPerfilFragment", "spinnerPerfil is not initialized or adapter is not set")
            return null
        }
        val selectedPerfilName = spinnerPerfil.selectedItem.toString()
        Log.d("SelectorPerfilFragment", "Perfil seleccionado: $selectedPerfilName")
        return perfiles.find { it.nombrePerfil == selectedPerfilName }
    }

    fun setSelectedPerfil(nombrePerfil: String) {
        if (perfiles.isNotEmpty()) {
            val index = perfiles.indexOfFirst { it.nombrePerfil == nombrePerfil }
            if (index >= 0) {
                spinnerPerfil.setSelection(index)
                Log.d("SelectorPerfilFragment", "Perfil establecido: $nombrePerfil en posición $index")
            }
        } else {
            pendingPerfilSelection = nombrePerfil
            Log.d("SelectorPerfilFragment", "Perfil pendiente: $nombrePerfil")
        }
    }

    // Listener to update selected profile when user selects an item
    private fun setupSpinnerListener() {
        spinnerPerfil.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val perfil = perfiles.getOrNull(position)
                _selectedPerfil.value = perfil
                Log.d("SelectorPerfilFragment", "Perfil seleccionado por usuario: ${perfil?.nombrePerfil}")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                _selectedPerfil.value = null
                Log.d("SelectorPerfilFragment", "Ningún perfil seleccionado")
            }
        }
        Log.d("SelectorPerfilFragment", "Listener del spinner configurado")
    }
}
