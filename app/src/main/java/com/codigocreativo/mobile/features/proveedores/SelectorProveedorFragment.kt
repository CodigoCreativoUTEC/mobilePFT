package com.codigocreativo.mobile.features.proveedores

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

class SelectorProveedorFragment : Fragment() {

    private lateinit var spinnerProveedor: Spinner
    private val dataRepository = DataRepository()
    private var proveedores: List<Proveedor> = emptyList()
    private var pendingSelection: String? = null

    // LiveData to observe the loading state
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_proveedor, container, false)
        spinnerProveedor = view.findViewById(R.id.spinnerProveedor)
        cargarProveedores()
        return view
    }

    private fun cargarProveedores() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val paisApiService = retrofit.create(ProveedoresApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    paisApiService.listarProveedores("Bearer $token")
                }

                result.onSuccess { proveedoresList ->
                    proveedores = proveedoresList
                    val nombresProveedores = proveedores.map { it.nombre }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresProveedores)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProveedor.adapter = adapter

                    // If there was a pending selection, set it now
                    pendingSelection?.let {
                        setSelectedProveedor(it)
                        pendingSelection = null
                    }

                    // Update the loading state
                    _isDataLoaded.value = true
                }.onFailure { exception ->
                    Log.e("SelectorProveedorFragment", "Error al cargar los proveedores", exception)
                }
            }
        }
    }

    fun getSelectedProveedor(): Proveedor? {
        val selectedProveedorName = spinnerProveedor.selectedItem.toString()
        return proveedores.find { it.nombre == selectedProveedorName }
    }

    fun setSelectedProveedor(nombreProveedor: String) {
        if (proveedores.isNotEmpty()) {
            val index = proveedores.indexOfFirst { it.nombre == nombreProveedor }
            if (index >= 0) {
                spinnerProveedor.setSelection(index)
            }
        } else {
            // If proveedores is not yet initialized, store the selection for later
            pendingSelection = nombreProveedor
        }
    }
}