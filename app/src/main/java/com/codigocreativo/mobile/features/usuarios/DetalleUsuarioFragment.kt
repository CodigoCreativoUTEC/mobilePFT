package com.codigocreativo.mobile.features.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_usuario, container, false)

        // Initialize views
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

        // Populate fields with data from the usuario object
        nombreInput.setText(usuario.nombre)
        apellidoInput.setText(usuario.apellido)
        fechaNacimientoInput.setText(usuario.fechaNacimiento)
        emailInput.setText(usuario.email)
        telefonoInput.setText(usuario.usuariosTelefonos?.joinToString(", ") { it.numero } ?: "")
        cedulaInput.setText(usuario.cedula)
        nombreUsuarioInput.setText(usuario.nombreUsuario)
        // Deshabilitar edición del nombre de usuario
        nombreUsuarioInput.isEnabled = false
        perfilPickerFragment.setSelectedPerfil(usuario.idPerfil.toString())

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        estadoSpinner.setSelection(Estado.values().indexOf(usuario.estado))

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

                // Create a list of Telefono objects preserving the original ID
                val originalPhoneId = usuario.usuariosTelefonos?.firstOrNull()?.id ?: 0
                val nuevosTelefonos = listOf(Telefono(
                    id = originalPhoneId,
                    numero = nuevoTelefono
                ))
                
                // Log the phone ID for debugging
                android.util.Log.d("DetalleUsuarioFragment", "Original phone ID: $originalPhoneId, New phone number: $nuevoTelefono")

                // Obtener la institución seleccionada
                val institucionSeleccionada = instituciones[institucionSpinner.selectedItemPosition]

                // Update the usuario object with new data
                val updatedUsuario = Usuario(
                    id = usuario.id,
                    nombre = nuevoNombre,
                    apellido = nuevoApellido,
                    fechaNacimiento = nuevoFechaNacimiento,
                    email = nuevoEmail,
                    contrasenia = usuario.contrasenia ?: "",  // Provide empty string if null
                    usuariosTelefonos = nuevosTelefonos,
                    cedula = nuevoCedula,
                    nombreUsuario = nuevoNombreUsuario,
                    idPerfil = nuevoPerfil,
                    idInstitucion = institucionSeleccionada,
                    estado = Estado.values()[estadoSpinner.selectedItemPosition]
                )

                // Call onEdit callback with updated Usuario object
                onEdit(updatedUsuario)
                dismiss()
            } else {
                Snackbar.make(view, "Por favor complete todos los campos", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}
