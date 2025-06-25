package com.codigocreativo.mobile.features.marca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class DetalleMarcaFragment(
    private val marca: Marca,
    private val onEdit: (Marca) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: TextInputEditText
    private lateinit var idInput: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_marca, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        idInput = view.findViewById(R.id.idInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the marca object
        idInput.setText(marca.id?.toString() ?: "")
        nombreInput.setText(marca.nombre)

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(marca.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()

            if (nuevoNombre.isNotBlank()) {
                val updatedMarca = Marca(
                    id = marca.id,
                    nombre = nuevoNombre,
                    estado = Estado.values()[estadoSpinner.selectedItemPosition]
                )
                onEdit(updatedMarca)
                dismiss()
            } else {
                Snackbar.make(view, "El nombre es obligatorio", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}


