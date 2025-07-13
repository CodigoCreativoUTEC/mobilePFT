package com.codigocreativo.mobile.features.paises

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class IngresarPaisFragment(private val onConfirm: (Pais) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button
    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_pais, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()

            if (nuevoNombre.isNotBlank()) {
                // Verificar si el país ya existe antes de crear
                checkIfPaisExists(nuevoNombre, view)
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un país", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }

    // Función para verificar si el país existe en la base de datos
    private fun checkIfPaisExists(nombre: String, view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(PaisApiService::class.java)

            lifecycleScope.launch {
                try {
                    // Realizamos la llamada a la API para filtrar por nombre
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.listarPaisesFiltradas("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { paises ->
                        // Depuración: Verificar la lista de países obtenidas
                        Log.d("IngresarPaisFragment", "Países obtenidos: ${paises.size}")

                        // Si ya existe un país con ese nombre
                        if (paises.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                            Snackbar.make(view, "El país '$nombre' ya existe en la base de datos", Snackbar.LENGTH_LONG).show()
                        } else {
                            // Si no existe, mostrar el diálogo de confirmación
                            showConfirmationDialog(nombre, view)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar el país: ${error.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("IngresarPaisFragment", "Error al verificar el país: ${error.message}")
                    }

                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
                    Log.e("IngresarPaisFragment", "Error: ${e.message}", e)
                }
            }
        }
    }

    private fun showConfirmationDialog(nombre: String, view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar creación")
            .setMessage("¿Desea crear el país '$nombre'?")
            .setPositiveButton("Confirmar") { _, _ ->
                val nuevoPais = Pais(
                    id = null,
                    nombre = nombre,
                    estado = Estado.ACTIVO
                )
                onConfirm(nuevoPais)
                dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
} 