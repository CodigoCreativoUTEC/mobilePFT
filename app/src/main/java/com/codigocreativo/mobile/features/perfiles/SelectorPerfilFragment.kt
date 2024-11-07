package com.codigocreativo.mobile.features.perfiles

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
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.SessionManager
import kotlinx.coroutines.launch

class SelectorPerfilFragment : Fragment() {

    private lateinit var spinnerPerfil: Spinner
    private val dataRepository = DataRepository()
    private var perfiles: List<Perfil> = emptyList()
    private var pendingPerfilSelection: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_perfil, container, false)
        spinnerPerfil = view.findViewById(R.id.spinnerPerfil)
        cargarPerfiles()
        return view
    }

    private fun cargarPerfiles() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val perfilApiService = retrofit.create(PerfilesApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    perfilApiService.listarPerfiles("Bearer $token")
                }

                result.onSuccess { perfilesList ->
                    perfiles = perfilesList
                    val nombresPerfiles = perfiles.map { it.nombrePerfil }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPerfiles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerPerfil.adapter = adapter

                    // If there was a pending perfil selection, set it now
                    pendingPerfilSelection?.let {
                        setSelectedPerfil(it)
                        pendingPerfilSelection = null
                    }
                }.onFailure { exception ->
                    Log.e("SelectorPerfilFragment", "Error al cargar los perfiles", exception)
                }
            }
        }
    }

    fun getSelectedPerfil(): Perfil? {
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
            // If perfiles is not yet initialized, store the selection for later
            pendingPerfilSelection = nombrePerfil
        }
    }
}