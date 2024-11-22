package com.codigocreativo.mobile.features.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.SessionManager

class PerfilUsuarioFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)

        // Cargar datos del usuario logueado
        val usuario = SessionManager.getUser(requireContext())
        if (usuario != null) {
            tvUserName.text = "Nombre: ${usuario.nombre}"
            tvUserEmail.text = "Correo: ${usuario.email}"
        }
    }

    companion object {
        fun newInstance(): PerfilUsuarioFragment {
            return PerfilUsuarioFragment()
        }
    }
}