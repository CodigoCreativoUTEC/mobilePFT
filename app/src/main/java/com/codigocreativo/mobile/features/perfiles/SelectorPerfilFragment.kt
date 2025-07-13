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
import kotlinx.coroutines.launch
import android.widget.AdapterView
import android.widget.Toast


class SelectorPerfilFragment : Fragment() {

    private companion object {
        private const val PERFIL_AUX_ADMINISTRATIVO = "Aux administrativo"
        private const val PERFIL_TECNICO = "Técnico"
    }

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
        
        // Configurar el listener para el spinner
        setupSpinnerListener()
        
        // Cargar perfiles desde la API
        cargarPerfilesDesdeAPI()
        
        return view
    }

    private fun cargarPerfilesDesdeAPI() {
        Log.d("SelectorPerfilFragment", "Cargando perfiles desde API...")
        
        // Mostrar indicador de carga si es necesario
        _isDataLoaded.value = false
        
        val retrofit = RetrofitClient.getClientSinToken()
        val perfilApiService = retrofit.create(PerfilApiService::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("SelectorPerfilFragment", "Realizando llamada a la API...")
                
                val result = dataRepository.obtenerDatosSinToken() {
                    perfilApiService.listarPerfilesSinToken()
                }

                result.onSuccess { perfilesList ->
                    Log.d("SelectorPerfilFragment", "Perfiles obtenidos exitosamente: ${perfilesList.size} perfiles")
                    
                    perfiles = perfilesList
                    
                    // Filtrar solo perfiles activos
                    val perfilesActivos = perfiles.filter { it.estado == com.codigocreativo.mobile.utils.Estado.ACTIVO }
                    
                    if (perfilesActivos.isEmpty()) {
                        Log.w("SelectorPerfilFragment", "No hay perfiles activos disponibles")
                        // Usar perfiles por defecto si no hay activos
                        perfiles = listOf(
                            Perfil(id = 1, nombrePerfil = "Administrador", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                            Perfil(id = 2, nombrePerfil = PERFIL_AUX_ADMINISTRATIVO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                            Perfil(id = 3, nombrePerfil = PERFIL_TECNICO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                            Perfil(id = 4, nombrePerfil = "Supervisor", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO)
                        )
                    } else {
                        perfiles = perfilesActivos
                    }

                    configurarSpinner()
                    
                    // Set pending selection if there was one
                    pendingPerfilSelection?.let {
                        setSelectedPerfil(it)
                        pendingPerfilSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                    Log.d("SelectorPerfilFragment", "Perfiles cargados exitosamente desde API")
                    
                }.onFailure { exception ->
                    Log.e("SelectorPerfilFragment", "Error al cargar los perfiles desde API", exception)
                    
                    // En caso de error, usar perfiles por defecto
                    perfiles = listOf(
                        Perfil(id = 1, nombrePerfil = "Administrador", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                        Perfil(id = 2, nombrePerfil = PERFIL_AUX_ADMINISTRATIVO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                        Perfil(id = 3, nombrePerfil = PERFIL_TECNICO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                        Perfil(id = 4, nombrePerfil = "Supervisor", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO)
                    )
                    
                    configurarSpinner()
                    
                    // Set pending selection if there was one
                    pendingPerfilSelection?.let {
                        setSelectedPerfil(it)
                        pendingPerfilSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                    
                    // Mostrar mensaje de error al usuario
                    Toast.makeText(requireContext(), "Error al cargar perfiles. Usando perfiles por defecto.", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("SelectorPerfilFragment", "Excepción durante la carga de perfiles", e)
                
                // En caso de excepción, usar perfiles por defecto
                perfiles = listOf(
                    Perfil(id = 1, nombrePerfil = "Administrador", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                    Perfil(id = 2, nombrePerfil = PERFIL_AUX_ADMINISTRATIVO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                    Perfil(id = 3, nombrePerfil = PERFIL_TECNICO, estado = com.codigocreativo.mobile.utils.Estado.ACTIVO),
                    Perfil(id = 4, nombrePerfil = "Supervisor", estado = com.codigocreativo.mobile.utils.Estado.ACTIVO)
                )
                
                configurarSpinner()
                
                // Set pending selection if there was one
                pendingPerfilSelection?.let {
                    setSelectedPerfil(it)
                    pendingPerfilSelection = null
                }

                // Update the loading state
                _isDataLoaded.value = true
                
                Toast.makeText(requireContext(), "Error de conexión. Usando perfiles por defecto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarSpinner() {
        Log.d("SelectorPerfilFragment", "Configurando spinner con ${perfiles.size} perfiles")
        
        val nombresPerfiles = perfiles.map { it.nombrePerfil }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPerfiles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPerfil.adapter = adapter

        Log.d("SelectorPerfilFragment", "Adapter configurado con ${nombresPerfiles.size} elementos")
        
        // Log de los perfiles disponibles
        perfiles.forEachIndexed { index, perfil ->
            Log.d("SelectorPerfilFragment", "Perfil $index: ID=${perfil.id}, Nombre=${perfil.nombrePerfil}, Estado=${perfil.estado}")
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
                // No se requiere acción cuando no hay selección en el spinner
                _selectedPerfil.value = null
                Log.d("SelectorPerfilFragment", "Ningún perfil seleccionado")
            }
        }
        Log.d("SelectorPerfilFragment", "Listener del spinner configurado")
    }
}
