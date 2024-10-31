package com.codigocreativo.mobile.features.tipoEquipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class IngresarTipoEquipoFragment(private val onConfirm: (TipoEquipo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button



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
                val tipoEquipo = TipoEquipo(id = null, nombreTipo = nuevoTipoEquipo, estado = Estado.ACTIVO)
                onConfirm(tipoEquipo)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un tipo de equipo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}