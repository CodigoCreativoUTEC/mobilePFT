package com.codigocreativo.mobile.features.equipos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.modelo.SelectorModeloFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.SelectorProveedorFragment
import com.codigocreativo.mobile.features.tipoEquipo.SelectorTipoEquipoFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

class DetalleEquipoFragment(
    private val equipo: Equipo,
    private val onEdit: (Equipo) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var modeloPickerFragment: SelectorModeloFragment
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var tipoEquipoPickerFragment: SelectorTipoEquipoFragment
    private lateinit var proveedorPickerFragment: SelectorProveedorFragment
    private lateinit var nroSerieInput: EditText
    private lateinit var garantiaInput: EditText
    private lateinit var fechaAdquisicionInput: EditText
    private lateinit var imagenImageView: ImageView
    private lateinit var idInput: TextView
    private lateinit var estadoSpinner: Spinner

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_equipo, container, false)

        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        modeloPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorModelo) as SelectorModeloFragment
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorPais) as SelectorPaisFragment
        tipoEquipoPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorTipoEquipo) as SelectorTipoEquipoFragment
        proveedorPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorProveedor) as SelectorProveedorFragment
        nroSerieInput = view.findViewById(R.id.serieInput)
        garantiaInput = view.findViewById(R.id.garantiaInput)
        fechaAdquisicionInput = view.findViewById(R.id.fechaAdquisicionInput)
        imagenImageView = view.findViewById(R.id.imagenImageView)
        idInput = view.findViewById(R.id.idInput)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)

        // Populate fields with data from the equipo object
        nombreInput.setText(equipo.nombre)
        modeloPickerFragment.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                modeloPickerFragment.setSelectedModelo(equipo.idModelo.nombre)
            }
        }
        paisPickerFragment.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                paisPickerFragment.setSelectedCountry(equipo.idPais.nombre)
            }
        }
        tipoEquipoPickerFragment.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                tipoEquipoPickerFragment.setSelectedTipo(equipo.idTipo.nombreTipo)
            }
        }
        proveedorPickerFragment.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                proveedorPickerFragment.setSelectedProveedor(equipo.idProveedor.nombre)
            }
        }
        nroSerieInput.setText(equipo.nroSerie)
        garantiaInput.setText(equipo.garantia.toString())
        fechaAdquisicionInput.setText(equipo.fechaAdquisicion.toString())
        idInput.text = equipo.id.toString()
        estadoSpinner.setSelection(Estado.entries.indexOf(equipo.estado))

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            Estado.entries.toTypedArray()
        )
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.entries.indexOf(equipo.estado))

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevoModelo = modeloPickerFragment.getSelectedModelo()
            val nuevoPais = paisPickerFragment.getSelectedCountry()
            val nuevoTipoEquipo = tipoEquipoPickerFragment.getSelectedTipo()
            val nuevoProveedor = proveedorPickerFragment.getSelectedProveedor()
            val nuevoNroSerie = nroSerieInput.text.toString()
            val nuevaGarantia = garantiaInput.text.toString()
            val nuevaFechaAdquisicion = fechaAdquisicionInput.text.toString()
            val nuevoEstado = Estado.entries[estadoSpinner.selectedItemPosition]

            if (nuevoNombre.isNotEmpty() && nuevoModelo != null && nuevoPais != null && nuevoTipoEquipo != null && nuevoProveedor != null) {
                val nuevoEquipo = Equipo(
                    equiposUbicaciones = emptyList(),
                    estado = nuevoEstado,
                    fechaAdquisicion = nuevaFechaAdquisicion,
                    garantia = nuevaGarantia,
                    id = equipo.id,
                    idInterno = equipo.idInterno,
                    idModelo = nuevoModelo,
                    idPais = nuevoPais,
                    idProveedor = nuevoProveedor,
                    idTipo = nuevoTipoEquipo,
                    ubicacion = equipo.ubicacion,
                    imagen = equipo.imagen,
                    nombre = nuevoNombre,
                    nroSerie = nuevoNroSerie
                )
                onEdit(nuevoEquipo)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para editar el Equipo", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}