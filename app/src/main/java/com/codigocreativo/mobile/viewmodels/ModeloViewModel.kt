package com.codigocreativo.mobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codigocreativo.mobile.features.modelo.Modelo
import com.codigocreativo.mobile.features.modelo.ModeloApiService
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModeloViewModel : ViewModel() {

    private val dataRepository = DataRepository()

    // MutableLiveData para observar cambios en los modelos
    val modelosList = MutableLiveData<List<Modelo>>()

    // Cargar modelos desde la API
    fun loadModelos(token: String) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.getClient(token)
                val apiService = retrofit.create(ModeloApiService::class.java)
                val call = apiService.listarModelos("Bearer $token")

                val result = withContext(Dispatchers.IO) {
                    dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { call }
                    )
                }

                result.onSuccess { modelos ->
                    modelosList.value = modelos // Actualiza la lista de modelos en LiveData
                }.onFailure { error ->
                    // Maneja errores aquí si es necesario
                }
            } catch (e: Exception) {
                // Maneja excepciones aquí si es necesario
            }
        }
    }

    // Obtener un modelo específico por ID
    fun getModeloById(id: Int): Modelo? {
        return modelosList.value?.find { it.id == id }
    }

    // Actualizar el estado de un modelo
    fun actualizarEstadoModelo(id: Int, nuevoEstado: Estado) {
        modelosList.value = modelosList.value?.map { modelo ->
            if (modelo.id == id) {
                modelo.estado = nuevoEstado
            }
            modelo
        }
    }
}
