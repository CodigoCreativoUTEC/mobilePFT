package com.codigocreativo.mobile.features.modelo

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
import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.features.marca.SelectorMarcaFragment
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class IngresarModeloFragment(private val onConfirm: (Modelo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var marcaPickerFragment: SelectorMarcaFragment

    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_modelo, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        marcaPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentMarcaPicker) as SelectorMarcaFragment

        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()
            val nuevoMarca = marcaPickerFragment.getSelectedMarca()

            if (nuevoNombre.isNotBlank() && nuevoMarca != null) {
                // Verificar si el modelo ya existe
                checkIfModeloExists(nuevoNombre, nuevoMarca, view)
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un nuevo modelo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }

    // Función para verificar si la marca existe en la base de datos
    private fun checkIfModeloExists(nombre: String, marca: Marca, view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(ModeloApiService::class.java)

            lifecycleScope.launch {
                try {
                    // Realizamos la llamada a la API
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.listarModelosFiltrados("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { modelos ->
                        // Depuración: Verificar la lista de marcas obtenidas
                        Log.d("IngresarModeloFragment", "Modelos obtenidos: ${modelos.size}")

                        // Si ya existe una marca con ese nombre
                        if (modelos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                            Snackbar.make(view, "El modelo '$nombre' ya existe en la base de datos", Snackbar.LENGTH_LONG).show()
                        } else {
                            // Si no existe, mostrar el diálogo de confirmación
                            showConfirmationDialog(nombre, marca, view)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar el modelo: ${error.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("IngresarModeloFragment", "Error al verificar el modelo: ${error.message}")
                    }

                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
                    Log.e("IngresarModeloFragment", "Error: ${e.message}", e)
                }
            }
        } else {
            Snackbar.make(view, "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }

    // Función para mostrar el diálogo de confirmación
    private fun showConfirmationDialog(nombre: String, marca : Marca, view: View) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
            .setMessage("¿Desea confirmar el ingreso del modelo '$nombre'?")
            .setPositiveButton("Confirmar") { _, _ ->
                // Crear el nuevo modelo y proceder
                val modelo = Modelo(id = null, nombre = nombre, idMarca = marca, estado = Estado.ACTIVO)
                onConfirm(modelo)  // Llamamos a la función de confirmación
                dismiss()  // Cerramos el BottomSheet
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()  // Cerrar el diálogo sin hacer nada
            }

        // Mostrar el diálogo
        builder.create().show()
    }
}