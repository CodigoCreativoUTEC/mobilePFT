package com.codigocreativo.mobile.features.proveedores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText


class IngresarProveedorFragment(private val onConfirm: (Proveedor) -> Unit) : BottomSheetDialogFragment() {


    private lateinit var tfNombre: EditText
    private lateinit var spinnerPais: Spinner
    private lateinit var btnConfirmar: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_proveedor, container, false)

        tfNombre = view.findViewById(R.id.tfNombre)
        spinnerPais = view.findViewById(R.id.spinnerPais)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)



        btnConfirmar.setOnClickListener {
            val nuevoNombre = tfNombre.text.toString()
            val nuevoPais = spinnerPais.selectedItem.toString()


            if (nuevoNombre.isNotBlank() && nuevoPais != null) {

               val proveedor = Proveedor(idProveedor = 0, nombre = nuevoNombre, pais = Pais(idPais = 1, nombre = "nuevoPais"), estado = Estado.ACTIVO)

                onConfirm(proveedor)
                dismiss() // Cerrar el bottom sheet
            } else {
                Snackbar.make(view, "Llene todos los campos para agregar un proveedor", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}