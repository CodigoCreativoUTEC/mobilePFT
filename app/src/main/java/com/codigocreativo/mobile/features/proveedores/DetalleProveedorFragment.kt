package com.codigocreativo.mobile.features.proveedores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class DetalleProveedorFragment(
    private val proveedor: Proveedor,
    private val onEdit: (Proveedor) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_proveedor, container, false)

        // Inicializar vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentPaisPicker) as SelectorPaisFragment
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the proveedor object
        nombreInput.setText(proveedor.nombre)

        // Configurar el selector de país
        proveedor.pais?.let { paisPickerFragment.setSelectedCountry(it.nombre) }

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(proveedor.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevoPais = paisPickerFragment.getSelectedCountry()

            if (nuevoNombre.isNotBlank() && nuevoPais != null) {
                val updatedProveedor = Proveedor(
                    idProveedor = proveedor.idProveedor,
                    nombre = nuevoNombre,
                    pais = nuevoPais,
                    estado = Estado.values()[estadoSpinner.selectedItemPosition]
                )
                // Mostrar diálogo de confirmación
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar cambios")
                    .setMessage("¿Desea guardar los cambios en el proveedor?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        onEdit(updatedProveedor)
                        dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Snackbar.make(view, "El nombre y país son obligatorios", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}