package com.codigocreativo.mobile.features.equipos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.equipos.Equipo
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class IngresarEquipoFragment(private val onConfirm: (Equipo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var tfNombre: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var paisPickerFragment: SelectorPaisFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_proveedor, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentPaisPicker) as SelectorPaisFragment

        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()
            val nuevoPais = paisPickerFragment.getSelectedCountry()

           /* if (nuevoNombre.isNotBlank() && nuevoPais != null) {
                val equipo = Equipo(idProveedor = null, nombre = nuevoNombre, pais = nuevoPais, estado = Estado.ACTIVO)
                onConfirm(equipo)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un proveedor", Snackbar.LENGTH_LONG).show()
            }*/
        }

        return view
    }
}