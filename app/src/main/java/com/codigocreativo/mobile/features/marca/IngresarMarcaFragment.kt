package com.codigocreativo.mobile.features.marca

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class IngresarMarcaFragment(private val onConfirm: (Marca) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_marca, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)


        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()


            if (nuevoNombre.isNotBlank()) {
                val marca = Marca(id = null, nombre = nuevoNombre,estado = Estado.ACTIVO)
                onConfirm(marca)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar una marca", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}