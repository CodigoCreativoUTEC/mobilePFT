package com.codigocreativo.mobile.features.equipos

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class DetalleEquipoFragment(
    private val equipo: Equipo,
    private val onEdit: (Equipo) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: TextInputEditText
    private lateinit var descripcionInput: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var estadoSpinner: Spinner
    private lateinit var imagenEquipo: ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_equipo, container, false)

        // Inicializar las vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        descripcionInput = view.findViewById(R.id.descripcionInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)
        imagenEquipo = view.findViewById(R.id.imagenEquipo)

        // Populate fields with data from the equipo object
        nombreInput.setText(equipo.nombre ?: "")
        descripcionInput.setText(equipo.descripcion ?: "")

        // Cargar la imagen del equipo
        cargarImagenEquipo()

        // Configurar click en la imagen para ver en pantalla completa
        view.findViewById<View>(R.id.imagenEquipo).parent.let { parent ->
            if (parent is View) {
                parent.setOnClickListener {
                    mostrarImagenPantallaCompleta()
                }
            }
        }

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Estado.entries.toTypedArray()
        )
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        equipo.estado?.let {
            estadoSpinner.setSelection(Estado.entries.indexOf(it))
        }

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevaDescripcion = descripcionInput.text.toString()
            val nuevoEstado = Estado.entries[estadoSpinner.selectedItemPosition]

            // Validar los campos obligatorios antes de editar el equipo
            if (nuevoNombre.isNotEmpty()) {
                val nuevoEquipo = Equipo(
                    equiposUbicaciones = equipo.equiposUbicaciones,
                    estado = nuevoEstado,
                    fechaAdquisicion = equipo.fechaAdquisicion,
                    garantia = equipo.garantia,
                    id = equipo.id,
                    idInterno = equipo.idInterno,
                    idModelo = equipo.idModelo,
                    idPais = equipo.idPais,
                    idProveedor = equipo.idProveedor,
                    idTipo = equipo.idTipo,
                    ubicacion = equipo.ubicacion,
                    imagen = equipo.imagen,
                    nombre = nuevoNombre,
                    nroSerie = equipo.nroSerie,
                    descripcion = nuevaDescripcion.takeIf { it.isNotBlank() }
                )
                onEdit(nuevoEquipo)
                dismiss()
            } else {
                Snackbar.make(view, "El nombre es obligatorio", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }

    private fun cargarImagenEquipo() {
        // Si el equipo tiene una imagen válida, cargarla con Glide
        if (!equipo.imagen.isNullOrBlank() && equipo.imagen != "null") {
            Glide.with(this)
                .load(equipo.imagen)
                .placeholder(R.drawable.equipos)
                .error(R.drawable.equipos)
                .centerCrop()
                .into(imagenEquipo)
        } else {
            // Si no hay imagen, mostrar la imagen por defecto
            imagenEquipo.setImageResource(R.drawable.equipos)
        }
    }

    private fun mostrarImagenPantallaCompleta() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val imageView = ImageView(requireContext())
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Cargar la imagen en pantalla completa
        if (!equipo.imagen.isNullOrBlank() && equipo.imagen != "null") {
            Glide.with(this)
                .load(equipo.imagen)
                .placeholder(R.drawable.equipos)
                .error(R.drawable.equipos)
                .fitCenter()
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.equipos)
        }

        dialog.setContentView(imageView)
        dialog.show()
    }
}
