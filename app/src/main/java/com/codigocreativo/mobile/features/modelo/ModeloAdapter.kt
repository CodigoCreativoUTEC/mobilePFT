package com.codigocreativo.mobile.features.modelo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R

class ModeloAdapter(
    private var modelosList: List<Modelo>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<ModeloAdapter.ModeloViewHolder>() {

    inner class ModeloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modelo, parent, false)
        return ModeloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        val modelo = modelosList[position]
        holder.idTextView.text = modelo.id.toString()
        holder.nombreTextView.text = modelo.nombre
        holder.estadoTextView.text = modelo.estado.name

        holder.btnDetalle.setOnClickListener {
            val fragment = DetalleModeloFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", modelo.id)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return modelosList.size
    }

    // Método para actualizar la lista de modelos y notificar al adaptador de los cambios
    fun updateList(newModelosList: List<Modelo>) {
        modelosList = newModelosList
        notifyDataSetChanged()
    }
}


