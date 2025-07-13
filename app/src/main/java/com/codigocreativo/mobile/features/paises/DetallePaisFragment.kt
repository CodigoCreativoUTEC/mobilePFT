package com.codigocreativo.mobile.features.paises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class DetallePaisFragment(
    private val pais: Pais,
    private val onEdit: (Pais) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_pais, container, false)

        // Inicializar vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the pais object
        nombreInput.setText(pais.nombre)

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(pais.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevoEstado = Estado.values()[estadoSpinner.selectedItemPosition]

            if (nuevoNombre.isNotBlank()) {
                val updatedPais = Pais(
                    id = pais.id,
                    nombre = nuevoNombre,
                    estado = nuevoEstado
                )
                // Mostrar diálogo de confirmación
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar cambios")
                    .setMessage("¿Desea guardar los cambios en el país?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        onEdit(updatedPais)
                        dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Snackbar.make(view, "El nombre es obligatorio", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
} 