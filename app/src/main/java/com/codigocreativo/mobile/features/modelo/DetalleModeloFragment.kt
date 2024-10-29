package com.codigocreativo.mobile.features.modelo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.SelectorMarcaFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class DetalleModeloFragment(
    private val modelo: Modelo,
    private val onEdit: (Modelo) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var marcaPickerFragment: SelectorMarcaFragment
    private lateinit var idInput: TextView
    private lateinit var estadoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_modelo, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        marcaPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentMarcaPicker) as SelectorMarcaFragment
        idInput = view.findViewById(R.id.idInput)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the modelo object
        idInput.text = modelo.id.toString()
        nombreInput.setText(modelo.nombre)
        marcaPickerFragment.setSelectedMarca(modelo.idMarca.nombre)


        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(modelo.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevoMarca = marcaPickerFragment.getSelectedMarca()


            if (nuevoNombre.isNotBlank() && nuevoMarca != null) {
                val updatedModelo = nuevoMarca?.let { it1 ->
                    Modelo(
                        id = modelo.id,
                        nombre = nuevoNombre,
                        idMarca = nuevoMarca,
                        estado = Estado.values()[estadoSpinner.selectedItemPosition]
                    )
                }
                if (updatedModelo != null) {
                    onEdit(updatedModelo)
                }
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para editar el modelo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}