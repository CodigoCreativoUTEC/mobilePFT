package com.codigocreativo.mobile.features.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.institucion.Institucion
import com.codigocreativo.mobile.features.perfiles.SelectorPerfilFragment
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class DetalleUsuarioFragment(
    private val usuario: Usuario,
    private val onEdit: (Usuario) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var idInput: TextView
    private lateinit var cedulaInput: EditText
    private lateinit var nombreInput: EditText
    private lateinit var apellidoInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var fechaNacimientoInput: EditText
    private lateinit var telefonoInput: EditText
    private lateinit var nombreUsuarioInput: EditText
    private lateinit var perfilPickerFragment: SelectorPerfilFragment
    private lateinit var btnConfirmar: Button
    private lateinit var estadoSpinner: Spinner
    private lateinit var institucionSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_usuario, container, false)

        val instituciones = listOf(
            Institucion(1, "CodigoCreativo"),
            Institucion(2, "Otra Institucion") // Agrega más según necesites
        )

        // Initialize views
        idInput = view.findViewById(R.id.idInput)
        nombreInput = view.findViewById(R.id.nombreInput)
        apellidoInput = view.findViewById(R.id.apellidoInput)
        fechaNacimientoInput = view.findViewById(R.id.fechaNacimientoInput)
        emailInput = view.findViewById(R.id.emailInput)
        telefonoInput = view.findViewById(R.id.telefonoInput)
        cedulaInput = view.findViewById(R.id.cedulaInput)
        nombreUsuarioInput = view.findViewById(R.id.nombreUsuarioInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        perfilPickerFragment = SelectorPerfilFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentPerfilPicker, perfilPickerFragment)
            .commit()
        estadoSpinner = view.findViewById(R.id.estadoSpinner)
        institucionSpinner = view.findViewById(R.id.institucionSpinner)

        // Populate fields with data from the usuario object
        idInput.text = usuario.id.toString()
        nombreInput.setText(usuario.nombre)
        apellidoInput.setText(usuario.apellido)
        fechaNacimientoInput.setText(usuario.fechaNacimiento)
        emailInput.setText(usuario.email)
        telefonoInput.setText(usuario.usuariosTelefonos?.joinToString(", ") { it.numero } ?: "")
        cedulaInput.setText(usuario.cedula)
        nombreUsuarioInput.setText(usuario.nombreUsuario)
        perfilPickerFragment.setSelectedPerfil(usuario.idPerfil.toString())

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.values().indexOf(usuario.estado))

        // Populate institucionSpinner with Institucion enum values
        val institucionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, instituciones.map { it.nombre })
        institucionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        institucionSpinner.adapter = institucionAdapter
        val currentInstitucionIndex = instituciones.indexOfFirst { it.id == usuario.idInstitucion.id }
        if (currentInstitucionIndex != -1) institucionSpinner.setSelection(currentInstitucionIndex)


        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString()
            val nuevoApellido = apellidoInput.text.toString()
            val nuevoFechaNacimiento = fechaNacimientoInput.text.toString()
            val nuevoEmail = emailInput.text.toString()
            val nuevoTelefono = telefonoInput.text.toString()
            val nuevoCedula = cedulaInput.text.toString()
            val nuevoNombreUsuario = nombreUsuarioInput.text.toString()
            val nuevoPerfil = perfilPickerFragment.getSelectedPerfil()


            if (nuevoNombre.isNotBlank() && nuevoApellido.isNotBlank() && nuevoFechaNacimiento.isNotBlank() &&
                nuevoEmail.isNotBlank() && nuevoTelefono.isNotBlank() && nuevoCedula.isNotBlank() &&
                nuevoNombreUsuario.isNotBlank() && nuevoPerfil != null) {

                // Create a list of Telefono objects with a single entry for simplicity
                val nuevosTelefonos = listOf(Telefono(id = 0, numero = nuevoTelefono))


                // Update the usuario object with new data
                val updatedUsuario = Usuario(
                    id = usuario.id,
                    nombre = nuevoNombre,
                    apellido = nuevoApellido,
                    fechaNacimiento = nuevoFechaNacimiento,
                    email = nuevoEmail,
                    contrasenia = usuario.contrasenia,  // Assuming contrasenia remains the same
                    usuariosTelefonos = nuevosTelefonos,
                    cedula = nuevoCedula,
                    nombreUsuario = nuevoNombreUsuario,
                    idPerfil = nuevoPerfil,
                    idInstitucion = com.codigocreativo.mobile.features.institucion.Institucion(1, "CodigoCreativo"),// TODO: HARDCODED institucion reparar
                    estado = Estado.values()[estadoSpinner.selectedItemPosition]
                )

                // Call onEdit callback with updated Usuario object
                onEdit(updatedUsuario)
                dismiss()
            } else {
                Snackbar.make(view, "Llene todos los campos para editar el usuario", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}
