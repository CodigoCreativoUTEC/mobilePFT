package com.codigocreativo.mobile.features.tipoEquipo

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
import com.codigocreativo.mobile.features.marca.MarcaApiService
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class IngresarTipoEquipoFragment(private val onConfirm: (TipoEquipo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button

    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_tipo_equipo, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)


        btnConfirmar.setOnClickListener {
            val nuevoTipoEquipo = tfNombre.text.toString()

            if (nuevoTipoEquipo.isNotBlank()) {
                // Verificar si el tipo de equipo ya existe antes de crear
                checkIfTipoEquipoExists(nuevoTipoEquipo, view)
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un tipo de equipo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
    // Función para verificar si la marca existe en la base de datos
    private fun checkIfTipoEquipoExists(nombre: String, view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(TipoEquipoApiService::class.java)

            lifecycleScope.launch {
                try {
                    // Realizamos la llamada a la API
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.listarTipoEquipos("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { tipoEquipos ->
                        // Depuración: Verificar la lista de tipo de equipos obtenidos
                        Log.d("IngresarTipoEquipoFragment", "TipoEquipo obtenidos: ${tipoEquipos.size}")

                        // Si ya existe un tipo de equipo con ese nombre
                        if (tipoEquipos.any { it.nombreTipo.equals(nombre, ignoreCase = true) }) {
                            Snackbar.make(view, "El tipo de equipo: '$nombre' ya existe en la base de datos", Snackbar.LENGTH_LONG).show()
                        } else {
                            // Si no existe, mostrar el diálogo de confirmación
                            showConfirmationDialog(nombre, view)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar el tipo de equipo: ${error.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("IngresarTipoEquipoFragment", "Error al verificar el tipo de Equipo: ${error.message}")
                    }

                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
                    Log.e("IngresarTipoEquipoFragment", "Error: ${e.message}", e)
                }
            }
        } else {
            Snackbar.make(view, "Token no encontrado, por favor inicia sesión", Snackbar.LENGTH_LONG).show()
        }
    }
    // Función para mostrar el diálogo de confirmación
    private fun showConfirmationDialog(nombre: String, view: View) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
            .setMessage("¿Desea confirmar el ingreso del tipo de equipo '$nombre'?")
            .setPositiveButton("Confirmar") { _, _ ->
                // Crear el nuevo tipo de equipo y proceder
                val tipoEquipo = TipoEquipo(id = null, nombreTipo = nombre, estado = Estado.ACTIVO)
                onConfirm(tipoEquipo)  // Llamamos a la función de confirmación
                dismiss()  // Cerramos el BottomSheet
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()  // Cerrar el diálogo sin hacer nada
            }

        // Mostrar el diálogo
        builder.create().show()
    }
}