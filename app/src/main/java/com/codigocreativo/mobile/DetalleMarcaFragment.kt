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
import androidx.appcompat.app.AlertDialog

class DetalleMarcaFragment : Fragment() {

    private lateinit var marcasList: List<Marca>  // La lista de marcas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_marca, container, false)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)

        // Obtener la lista de marcas desde la actividad (o puedes pasarla desde el Bundle si es necesario)
        marcasList = (activity as MarcasActivity).marcasList

        // Obtener el ID de la marca seleccionada
        val marcaId = arguments?.getInt("marca_id") ?: 0
        val marca = getMarcaById(marcaId)

        // Setear los valores de la marca seleccionada
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
            // Crear el diálogo de confirmación para editar
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar edición")
                .setMessage("¿Está seguro que desea editar el estado de la marca ${marca.nombre}?")
                .setPositiveButton("Aceptar") { _, _ ->
                    // Actualizar el estado de la marca (simulado)
                    marca.estado = estadoSpinner.selectedItem as Estado
                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)  // No hacer nada si se cancela
                .show()
        }


        // Botón Volver
        val btnVolver: Button = view.findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            // Volver a la lista de marcas
            fragmentManager?.popBackStack()
        }

        return view
    }

    // Buscar la marca por ID en la lista de marcas
    private fun getMarcaById(id: Int): Marca {
        return marcasList.first { it.id == id }
    }
}


