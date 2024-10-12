package com.codigocreativo.mobile.features.modelo

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
import com.codigocreativo.mobile.viewmodels.ModeloViewModel

class DetalleModeloFragment : Fragment() {

    private lateinit var viewModel: ModeloViewModel
    private var modeloId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_detalle_modelo, container, false)

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ModeloViewModel::class.java)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val nombreMarcaTextView: TextView = view.findViewById(R.id.nombreMarcaTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnVolver: Button = view.findViewById(R.id.btnVolver)

        // Obtener el ID del modelo desde los argumentos
        modeloId = arguments?.getInt("id") ?: 0
        Log.d("DetalleModeloFragment", "ID del modelo recibido: $modeloId")

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
        viewModel.modelosList.observe(viewLifecycleOwner) { modelos ->
            Log.d("DetalleModeloFragment", "Modelos recibidos: ${modelos.map { it.id }}")

            val modelo = modelos.find { it.id == modeloId }
            if (modelo != null) {
                Log.d("DetalleModeloFragment", "Mostrando modelo con ID: ${modelo.id}")
                idTextView.text = modelo.id.toString()
                nombreTextView.text = modelo.nombre
                nombreMarcaTextView.text = modelo.idMarca.nombre
                estadoSpinner.setSelection(statusAdapter.getPosition(modelo.estado))
            } else {
                Log.e("DetalleModeloFragment", "No se encontró el modelo con ID: $modeloId")
            }
        }

        // Acción al hacer clic en el botón de editar
        btnEditar.setOnClickListener {
            val nuevoEstado = estadoSpinner.selectedItem as Estado
            viewModel.actualizarEstadoModelo(modeloId, nuevoEstado)
            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
        }

        // Acción al hacer clic en el botón de volver
        btnVolver.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return view
    }
}
