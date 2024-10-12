package com.codigocreativo.mobile.features.proveedores

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
import com.codigocreativo.mobile.viewmodels.ProveedorViewModel

class DetalleProveedorFragment : Fragment() {

    private lateinit var viewModel: ProveedorViewModel
    private var proveedorId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_detalle_proveedor, container, false)

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ProveedorViewModel::class.java)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val nombrePaisTextView: TextView = view.findViewById(R.id.nombrePaisTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnVolver: Button = view.findViewById(R.id.btnVolver)

        // Obtener el ID del proveedor desde los argumentos
        proveedorId = arguments?.getInt("id") ?: 0
        Log.d("DetalleModeloFragment", "ID del proveedor recibido: $proveedorId")

        // Obtener el token desde SessionManager usando el contexto del fragmento
        val token = SessionManager.getToken(requireContext())

        if (token != null) {
            // Llama al método para cargar los proveedors
            viewModel.loadproveedores(token)
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

        // Observar los cambios en la lista de proveedors
        viewModel.proveedorList.observe(viewLifecycleOwner) { proveedors ->
            Log.d("DetalleModeloFragment", "Modelos recibidos: ${proveedors.map { it.idProveedor }}")

            val proveedor = proveedors.find { it.idProveedor == proveedorId }
            if (proveedor != null) {
                Log.d("DetalleModeloFragment", "Mostrando proveedor con ID: ${proveedor.idProveedor}")
                idTextView.text = proveedor.idProveedor.toString()
                nombreTextView.text = proveedor.nombre
                nombrePaisTextView.text = proveedor.pais.nombre
                estadoSpinner.setSelection(statusAdapter.getPosition(proveedor.estado))
            } else {
                Log.e("DetalleModeloFragment", "No se encontró el proveedor con ID: $proveedorId")
            }
        }

        // Acción al hacer clic en el botón de editar
        btnEditar.setOnClickListener {
            val nuevoEstado = estadoSpinner.selectedItem as Estado
            viewModel.actualizarEstadoProveedor(proveedorId, nuevoEstado)
            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
        }

        // Acción al hacer clic en el botón de volver
        btnVolver.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return view
    }
}
