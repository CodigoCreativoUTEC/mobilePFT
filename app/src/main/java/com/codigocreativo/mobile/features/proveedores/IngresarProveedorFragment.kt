package com.codigocreativo.mobile.features.proveedores

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
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class IngresarProveedorFragment(private val onConfirm: (Proveedor) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var paisPickerFragment: SelectorPaisFragment

    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_proveedor, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentPaisPicker) as SelectorPaisFragment

        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()
            val nuevoPais = paisPickerFragment.getSelectedCountry()

            if (nuevoNombre.isNotBlank() && nuevoPais != null) {
                // Verificar si el proveedor ya existe antes de crear
                checkIfProveedorExists(nuevoNombre, view)
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un proveedor", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
    // Función para verificar si el proveedor existe en la base de datos
    private fun checkIfProveedorExists(nombre: String, view: View) {
        val nuevoPais = paisPickerFragment.getSelectedCountry()
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(ProveedoresApiService::class.java)

            lifecycleScope.launch {
                try {
                    // Realizamos la llamada a la API
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.buscarProveedores("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { proveedores ->
                        // Depuración: Verificar la lista de proveedores obtenidas
                        Log.d("IngresarProveedorFragment", "Proveedores obtenidos: ${proveedores.size}")

                        // Si ya existe un proveedor con ese nombre
                        if (proveedores.isNotEmpty()) {
                            Snackbar.make(view, "El proveedor '$nombre' ya existe en la base de datos", Snackbar.LENGTH_LONG).show()
                        } else {
                            // Si no existe, mostrar el diálogo de confirmación
                            showConfirmationDialog(nombre, nuevoPais, view)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar el proveedor: ${error.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("IngresarProveedorFragment", "Error al verificar el proveedor: ${error.message}")
                    }

                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
                    Log.e("IngresarProveedorFragment", "Error: ${e.message}", e)
                }
            }
        } else {
            Snackbar.make(view, "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }

    // Función para mostrar el diálogo de confirmación
    private fun showConfirmationDialog(nombre: String, pais: Pais?, view: View) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar Creación")
            .setMessage("¿Está seguro que desea crear el proveedor '$nombre' del país '${pais?.nombre ?: "No especificado"}'?")
            .setPositiveButton("Confirmar") { _, _ ->
                // Crear el nuevo proveedor y proceder
                val proveedor = Proveedor(idProveedor = null, nombre = nombre, pais = pais, estado = Estado.ACTIVO)
                onConfirm(proveedor)  // Llamamos a la función de confirmación
                dismiss()  // Cerramos el BottomSheet
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()  // Cerrar el diálogo sin hacer nada
            }

        // Mostrar el diálogo
        builder.create().show()
    }
}