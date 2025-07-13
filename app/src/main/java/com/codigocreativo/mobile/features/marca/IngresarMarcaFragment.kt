package com.codigocreativo.mobile.features.marca


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

class IngresarMarcaFragment(private val onConfirm: (Marca) -> Unit) : BottomSheetDialogFragment() {

    companion object {
        private const val TOKEN_NOT_FOUND_MESSAGE = "Token no encontrado, por favor inicia sesión"
    }

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button

    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_marca, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()

            if (nuevoNombre.isNotBlank()) {
                // Verificar si la marca ya existe
                checkIfMarcaExists(nuevoNombre, view)
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar una marca", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }

    // Función para verificar si la marca existe en la base de datos
    private fun checkIfMarcaExists(nombre: String, view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(MarcaApiService::class.java)

            lifecycleScope.launch {
                try {
                    // Realizamos la llamada a la API
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.listarMarcasFiltradas("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { marcas ->
                        // Depuración: Verificar la lista de marcas obtenidas
                        Log.d("IngresarMarcaFragment", "Marcas obtenidas: ${marcas.size}")

                        // Si ya existe una marca con ese nombre
                        if (marcas.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                            Snackbar.make(view, "La marca '$nombre' ya existe en la base de datos", Snackbar.LENGTH_LONG).show()
                        } else {
                            // Si no existe, mostrar el diálogo de confirmación
                            showConfirmationDialog(nombre)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar la marca: ${error.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("IngresarMarcaFragment", "Error al verificar la marca: ${error.message}")
                    }

                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
                    Log.e("IngresarMarcaFragment", "Error: ${e.message}", e)
                }
            }
        } else {
            Snackbar.make(view, TOKEN_NOT_FOUND_MESSAGE, Snackbar.LENGTH_LONG).show()
        }
    }

    // Función para mostrar el diálogo de confirmación
    private fun showConfirmationDialog(nombre: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
            .setMessage("¿Desea confirmar el ingreso de la marca '$nombre'?")
            .setPositiveButton("Confirmar") { _, _ ->
                // Crear la nueva marca y proceder
                val marca = Marca(id = null, nombre = nombre, estado = Estado.ACTIVO)
                onConfirm(marca)  // Llamamos a la función de confirmación
                dismiss()  // Cerramos el BottomSheet
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()  // Cerrar el diálogo sin hacer nada
            }

        // Mostrar el diálogo
        builder.create().show()
    }
}

