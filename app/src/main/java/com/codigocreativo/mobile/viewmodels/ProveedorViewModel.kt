package com.codigocreativo.mobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.proveedores.ProveedoresApiService
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProveedorViewModel : ViewModel() {

    private val dataRepository = DataRepository()

    // MutableLiveData para observar cambios en los proveedores
    val proveedorList = MutableLiveData<List<Proveedor>>()

    // Cargar proveedores desde la API
    fun loadproveedores(token: String) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(ProveedoresApiService::class.java)
                val call = apiService.listarProveedores("Bearer $token")

                val result = withContext(Dispatchers.IO) {
                    dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { call }
                    )
                }

                result.onSuccess { proveedores ->
                    proveedorList.value = proveedores // Actualiza la lista de proveedores en LiveData
                }.onFailure { error ->
                    // Maneja errores aquí si es necesario
                }
            } catch (e: Exception) {
                // Maneja excepciones aquí si es necesario
            }
        }
    }

    // Obtener un proveedor específico por ID
    fun getProveedorById(id: Int): Proveedor? {
        return proveedorList.value?.find { it.idProveedor == id }
    }

    // Actualizar el estado de un proveedor
    fun actualizarEstadoProveedor(id: Int, nuevoEstado: Estado) {
        proveedorList.value = proveedorList.value?.map { proveedor ->
            if (proveedor.idProveedor == id) {
                proveedor.estado = nuevoEstado
            }
            proveedor
        }
    }
}
