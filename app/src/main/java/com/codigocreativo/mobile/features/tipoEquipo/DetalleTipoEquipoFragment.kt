package com.codigocreativo.mobile.features.tipoEquipo

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
import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class DetalleTipoEquipoFragment(
    private val tipoEquipo: TipoEquipo,
    private val onEdit: (TipoEquipo) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var idInput: TextView
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_tipo_equipo, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        idInput = view.findViewById(R.id.idInput)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the marca object
        idInput.text = tipoEquipo.id.toString()
        nombreInput.setText(tipoEquipo.nombreTipo)


        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(tipoEquipo.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()


            if (nuevoNombre.isNotBlank()) {
                val updatedTipoEquipo = TipoEquipo(
                    id = tipoEquipo.id,
                    nombreTipo = nuevoNombre,
                    estado = Estado.values()[estadoSpinner.selectedItemPosition]
                )
                onEdit(updatedTipoEquipo)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para editar el tipo de equipo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}