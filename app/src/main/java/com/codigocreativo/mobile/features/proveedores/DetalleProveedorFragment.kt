package com.codigocreativo.mobile.features.proveedores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class DetalleProveedorFragment(
    private val proveedor: Proveedor,
    private val onEdit: (Proveedor) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var idInput: TextView
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_proveedor, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentPaisPicker) as SelectorPaisFragment
        idInput = view.findViewById(R.id.idInput)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the proveedor object
        idInput.text = proveedor.idProveedor.toString()
        nombreInput.setText(proveedor.nombre)
        paisPickerFragment.setSelectedCountry(proveedor.pais.nombre)

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
                onEdit(updatedProveedor)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para editar el proveedor", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}