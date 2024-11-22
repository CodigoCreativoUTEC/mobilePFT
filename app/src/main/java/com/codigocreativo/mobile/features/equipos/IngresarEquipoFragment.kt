package com.codigocreativo.mobile.features.equipos

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R

import com.codigocreativo.mobile.features.modelo.SelectorModeloFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.SelectorProveedorFragment
import com.codigocreativo.mobile.features.tipoEquipo.SelectorTipoEquipoFragment
import com.codigocreativo.mobile.features.ubicacion.SelectorUbicacionFragment
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class IngresarEquipoFragment(private val onConfirm: (Equipo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var modeloPickerFragment: SelectorModeloFragment
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var tipoEquipoPickerFragment: SelectorTipoEquipoFragment
    private lateinit var proveedorPickerFragment: SelectorProveedorFragment
    private lateinit var ubicacionPickerFragment: SelectorUbicacionFragment
    private lateinit var nroSerieInput: EditText
    private lateinit var garantiaInput: EditText
    private lateinit var fechaAdquisicionInput: EditText
    private lateinit var imagenImageView: ImageView

    private val dataRepository = DataRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_equipo, container, false)

        // Inicializar vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        modeloPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorModelo) as SelectorModeloFragment
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorPais) as SelectorPaisFragment
        tipoEquipoPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorTipoEquipo) as SelectorTipoEquipoFragment
        proveedorPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorProveedor) as SelectorProveedorFragment
        ubicacionPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorUbicacion) as SelectorUbicacionFragment
        nroSerieInput = view.findViewById(R.id.serieInput)
        garantiaInput = view.findViewById(R.id.garantiaInput)
        fechaAdquisicionInput = view.findViewById(R.id.fechaAdquisicionInput)
        imagenImageView = view.findViewById(R.id.imagenImageView)

        btnConfirmar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            if (nombre.isEmpty()) {
                Snackbar.make(view, "El nombre del equipo es obligatorio", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val modelo = modeloPickerFragment.getSelectedModelo()
            val pais = paisPickerFragment.getSelectedCountry()
            val tipoEquipo = tipoEquipoPickerFragment.getSelectedTipo()
            val proveedor = proveedorPickerFragment.getSelectedProveedor()
            val ubicacion = ubicacionPickerFragment.getSelectedUbicacion()

            if (modelo == null || pais == null || tipoEquipo == null || proveedor == null || ubicacion == null) {
                Snackbar.make(view, "Por favor selecciona todos los datos requeridos", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val nroSerie = nroSerieInput.text.toString().trim()
            val garantia = garantiaInput.text.toString().takeIf { it.isNotEmpty() } ?: "Sin garantía"
            val fechaAdquisicion = fechaAdquisicionInput.text.toString().takeIf { it.isNotEmpty() } ?: "2024-01-01"
            val imagen = "https://via.placeholder.com/150" // Imagen por defecto
            val estado = Estado.ACTIVO

            val equipo = Equipo(
                id = null,
                nombre = nombre,
                idModelo = modelo,
                estado = estado,
                equiposUbicaciones = emptyList(),
                fechaAdquisicion = fechaAdquisicion,
                garantia = garantia,
                idInterno = null.toString(),
                idPais = pais,
                idProveedor = proveedor,
                idTipo = tipoEquipo,
                imagen = imagen,
                nroSerie = nroSerie,
                ubicacion = ubicacion
            )

            Log.d("IngresarEquipoFragment", "Equipo creado: $equipo")
            onConfirm(equipo)
            dismiss()
        }

        return view
    }

    // Función para verificar si el equipo ya existe
    private fun checkIfEquipoExists(nombre: String, view: View) {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(EquiposApiService::class.java)

            lifecycleScope.launch {
                try {
                    val result = dataRepository.obtenerDatos(
                        token = token,
                        apiCall = { apiService.buscar("Bearer $token", nombre, null) }
                    )

                    result.onSuccess { equipos ->
                        Log.d("IngresarEquipoFragment", "Equipos encontrados: ${equipos.size}")
                        if (equipos.any { it.nombre.equals(nombre, ignoreCase = true) }) {
                            Snackbar.make(view, "El equipo '$nombre' ya existe", Snackbar.LENGTH_LONG).show()
                        } else {
                            showConfirmationDialog(nombre, view)
                        }
                    }.onFailure { error ->
                        Snackbar.make(view, "Error al verificar el equipo: ${error.message}", Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(view, "Error inesperado: ${e.message}", Snackbar.LENGTH_LONG).show()
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
            .setMessage("¿Desea confirmar el ingreso del equipo '$nombre'?")
            .setPositiveButton("Confirmar") { _, _ ->
                Snackbar.make(view, "Equipo confirmado: $nombre", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}
