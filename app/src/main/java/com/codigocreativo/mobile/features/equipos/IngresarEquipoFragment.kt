package com.codigocreativo.mobile.features.equipos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.modelo.SelectorModeloFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.SelectorProveedorFragment
import com.codigocreativo.mobile.features.tipoEquipo.SelectorTipoEquipoFragment
import com.codigocreativo.mobile.features.ubicacion.SelectorUbicacionFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IngresarEquipoFragment(private val onConfirm: (Equipo) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var modeloPickerFragment: SelectorModeloFragment
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var tipoEquipoPickerFragment: SelectorTipoEquipoFragment
    private lateinit var proveedorPickerFragment: SelectorProveedorFragment
    private lateinit var ubicacionPickerFragment: SelectorUbicacionFragment
    private lateinit var nroSerieInput: EditText
    private lateinit var garantiaInput: EditText
    private lateinit var fechaAdquisicionInput: EditText
    private lateinit var imagenImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_equipo, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        modeloPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorModelo) as SelectorModeloFragment
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorPais) as SelectorPaisFragment
        tipoEquipoPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorTipoEquipo) as SelectorTipoEquipoFragment
        proveedorPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorProveedor) as SelectorProveedorFragment
        ubicacionPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorUbicacion) as SelectorUbicacionFragment
        nroSerieInput = view.findViewById(R.id.serieInput)
        garantiaInput = view.findViewById(R.id.garantiaInput)
        fechaAdquisicionInput = view.findViewById(R.id.fechaAdquisicionInput)
        imagenImageView = view.findViewById(R.id.imagenImageView)

        btnConfirmar.setOnClickListener {
            val nombre = nombreInput.text.toString()
            val modelo = modeloPickerFragment.getSelectedModelo()
            val pais = paisPickerFragment.getSelectedCountry()
            val tipoEquipo = tipoEquipoPickerFragment.getSelectedTipo()
            val proveedor = proveedorPickerFragment.getSelectedProveedor()
            val nroSerie = nroSerieInput.text.toString()
            //val garantia = garantiaInput.text.toString()
            val garantia = "2024-12-12"
            //val fechaAdquisicion = fechaAdquisicionInput.text.toString()
            val fechaAdquisicion = "2022-01-01"
            val ubicacion = ubicacionPickerFragment.getSelectedUbicacion()
            val imagen = "https://via.placeholder.com/150"
            val estado = Estado.ACTIVO

            val equipo = Equipo(
                emptyList(),
                estado,
                fechaAdquisicion,
                garantia,
                null,
                nroSerie,
                modelo!!,
                pais!!,
                proveedor!!,
                tipoEquipo!!,
                ubicacion!!,
                imagen,
                nombre,
                nroSerie
            )
            Log.d("IngresarEquipoFragment", "Equipo: $equipo")
            onConfirm(equipo)
            dismiss()
        }

        return view
    }
}