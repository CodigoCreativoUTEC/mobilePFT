package com.codigocreativo.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast

class DetalleMarcaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_marca, container, false)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)

        // Obtener el ID de la marca seleccionada
        val marcaId = arguments?.getInt("marca_id") ?: 0
        val marca = getMarcaById(marcaId)

        // Setear los valores
        idTextView.text = marca.id.toString()
        nombreTextView.text = marca.nombre

        // Spinner de estado
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = statusAdapter
        estadoSpinner.setSelection(statusAdapter.getPosition(marca.estado))

        // Botón Editar
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        btnEditar.setOnClickListener {
            // Actualizar el estado de la marca (simulado)
            marca.estado = estadoSpinner.selectedItem as Estado
            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
        }

        // Botón Volver
        val btnVolver: Button = view.findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            // Volver a la lista de marcas
            fragmentManager?.popBackStack()
        }

        return view
    }

    // Simulación de obtención de los datos de la marca
    private fun getMarcaById(id: Int): Marca {
        return Marca(id, "Toyota", Estado.ACTIVO)
    }
}

