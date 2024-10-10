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
import com.codigocreativo.mobile.objetos.Estado
import com.codigocreativo.mobile.objetos.Modelo

class DetalleModeloFragment : Fragment() {

    private lateinit var modelosList: List<Modelo>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_detalle_modelo, container, false)

        val idTextView: TextView = view.findViewById(R.id.idTextView)
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val estadoSpinner: Spinner = view.findViewById(R.id.estadoSpinner)

        modelosList = (activity as ModelosActivity).modelosList

        val modeloId = arguments?.getInt("modelo_id") ?: 0
        val modelo = getModeloById(modeloId)

        idTextView.text = modelo.id.toString()
        nombreTextView.text = modelo.nombre

        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Estado.values())
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = statusAdapter
        estadoSpinner.setSelection(statusAdapter.getPosition(modelo.estado))

        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        btnEditar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar edición")
                .setMessage("¿Está seguro que desea editar el estado del modelo ${modelo.nombre}?")
                .setPositiveButton("Aceptar") { _, _ ->
                    modelo.estado = estadoSpinner.selectedItem as Estado
                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        val btnVolver: Button = view.findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return view
    }

    private fun getModeloById(id: Int): Modelo {
        return modelosList.first { it.id == id }
    }
}


