package com.codigocreativo.mobile.features.modelo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.SelectorMarcaFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class IngresarModeloFragment(private val onConfirm: (Modelo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var marcaPickerFragment: SelectorMarcaFragment


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

                val modelo = Modelo(id = null, nombre = nuevoNombre, idMarca= nuevoMarca , estado = Estado.ACTIVO)
                onConfirm(modelo)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un modelo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}