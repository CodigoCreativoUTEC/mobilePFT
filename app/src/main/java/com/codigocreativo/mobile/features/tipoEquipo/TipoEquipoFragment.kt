package com.codigocreativo.mobile.features.tipoEquipo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.codigocreativo.mobile.viewmodels.TipoEquipoViewModel

class TipoEquipoFragment : Fragment() {

    private lateinit var viewModel: TipoEquipoViewModel
    private var tipoEquipoId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_detalle_tipo_equipo, container, false)

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(TipoEquipoViewModel::class.java)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnVolver: Button = view.findViewById(R.id.btnVolver)

        // Obtener el ID del modelo desde los argumentos
        tipoEquipoId = arguments?.getInt("id") ?: 0
        Log.d("DetalleModeloFragment", "ID del modelo recibido: $tipoEquipoId")

        // Obtener el token desde SessionManager usando el contexto del fragmento
        val token = SessionManager.getToken(requireContext())

        if (token != null) {
            // Llama al método para cargar los modelos
            viewModel.loadModelos(token)
        } else {
            Log.e("DetalleModeloFragment", "Token no encontrado.")
        }

        // Configurar el adapter para el spinner de estado
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Estado.values()
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = statusAdapter

        // Observar los cambios en la lista de modelos
        viewModel.tipoEquipoList.observe(viewLifecycleOwner) { modelos ->
            Log.d("DetalleModeloFragment", "Modelos recibidos: ${modelos.map { it.id }}")

            val tipoEquipo = modelos.find { it.id == tipoEquipoId }
            if (tipoEquipo != null) {
                Log.d("DetalleModeloFragment", "Mostrando modelo con ID: ${tipoEquipo.id}")
                idTextView.text = tipoEquipo.id.toString()
                nombreTextView.text = tipoEquipo.nombreTipo
                estadoSpinner.setSelection(statusAdapter.getPosition(tipoEquipo.estado))
            } else {
                Log.e("DetalleModeloFragment", "No se encontró el modelo con ID: $tipoEquipoId")
            }
        }

        // Acción al hacer clic en el botón de editar
        btnEditar.setOnClickListener {
            val nuevoEstado = estadoSpinner.selectedItem as Estado
            viewModel.actualizarEstadoModelo(tipoEquipoId, nuevoEstado)
            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
        }

        // Acción al hacer clic en el botón de volver
        btnVolver.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return view
    }
}
