package com.codigocreativo.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MarcaAdapter(private var marcasList: MutableList<Marca>) : RecyclerView.Adapter<MarcaAdapter.MarcaViewHolder>() {

    // Clase interna para el ViewHolder
    class MarcaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarcaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marca, parent, false)
        return MarcaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarcaViewHolder, position: Int) {
        val marca = marcasList[position]
        holder.idTextView.text = marca.id.toString()
        holder.nombreTextView.text = marca.nombre
        holder.estadoTextView.text = marca.estado.name
    }

    override fun getItemCount(): Int {
        return marcasList.size
    }

    // Actualizar la lista de marcas después de aplicar un filtro
    fun updateList(newList: MutableList<Marca>) {
        marcasList = newList
        notifyDataSetChanged()
    }
}
