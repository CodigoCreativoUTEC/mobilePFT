package com.codigocreativo.mobile.features.usuarios

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.codigocreativo.mobile.R

class PerfilUsuarioFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var cedulaEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var btnSave: Button
    private val IMAGE_PICK_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_ver_perfil, container, false)

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image)
        usernameTextView = view.findViewById(R.id.username)
        nombreEditText = view.findViewById(R.id.edit_nombre)
        apellidoEditText = view.findViewById(R.id.edit_apellido)
        cedulaEditText = view.findViewById(R.id.edit_cedula)
        emailEditText = view.findViewById(R.id.edit_email)
        telefonoEditText = view.findViewById(R.id.edit_telefono)
        btnSave = view.findViewById(R.id.btn_save)

        // Load user data
        loadUserData()

        // Set click listener for profile image
        profileImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    IMAGE_PICK_CODE
                )
            }
        }

        // Set click listener for save button
        btnSave.setOnClickListener {
            saveUserData()
        }

        return view
    }

    private fun loadUserData() {
        // Placeholder for getting logged-in user data
        val loggedUser = getLoggedInUser()

        // Set user data on fields
        usernameTextView.text = loggedUser.nombreUsuario
        nombreEditText.setText(loggedUser.nombre)
        apellidoEditText.setText(loggedUser.apellido)
        cedulaEditText.setText(loggedUser.cedula)
        emailEditText.setText(loggedUser.email)
        telefonoEditText.setText(loggedUser.telefono)
    }

    private fun saveUserData() {
        // Save user data logic (e.g., update to database or API)
        val updatedUser = Usuario(
            nombreUsuario = usernameTextView.text.toString(),
            nombre = nombreEditText.text.toString(),
            apellido = apellidoEditText.text.toString(),
            cedula = cedulaEditText.text.toString(),
            email = emailEditText.text.toString(),
            telefono = telefonoEditText.text.toString()
        )

        // Assume updateUser function is implemented to update user data
        updateUser(updatedUser)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            profileImage.setImageURI(data?.data) // Set selected image to profile image view
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_PICK_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }

    private fun getLoggedInUser(): Usuario {
        // Placeholder for getting the currently logged-in user
        // Replace with your logic to fetch the logged-in user
        return Usuario(
            nombreUsuario = "usuarioEjemplo",
            nombre = "NombreEjemplo",
            apellido = "ApellidoEjemplo",
            cedula = "12345678",
            email = "usuario@ejemplo.com",
            telefono = "123456789"
        )
    }

    private fun updateUser(user: Usuario) {
        // Placeholder for updating user information
        // Replace with your logic to save the user data
    }

    // Placeholder user data class
    data class Usuario(
        val nombreUsuario: String,
        val nombre: String,
        val apellido: String,
        val cedula: String,
        val email: String,
        val telefono: String
    )
}
