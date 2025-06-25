package com.codigocreativo.mobile.features.tipoEquipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.Marca

class TipoEquipoAdapter(
    var tipoEquipoList: List<TipoEquipo>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (TipoEquipo) -> Unit
) : RecyclerView.Adapter<TipoEquipoAdapter.TipoEquipoViewHolder>() {

    inner class TipoEquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTipoEquipoTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTipoEquipoTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipoEquipoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tipo_equipo, parent, false)
        return TipoEquipoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipoEquipoViewHolder, position: Int) {
        val tipoEquipo = tipoEquipoList[position]

        holder.nombreTextView.text = tipoEquipo.nombreTipo
        holder.estadoTextView.text = tipoEquipo.estado.name

        holder.itemView.setOnClickListener {
            onDetalleClick(tipoEquipo)
        }
    }

    override fun getItemCount(): Int {
        return tipoEquipoList.size
    }

    fun updateList(newTipoEquipoList: List<TipoEquipo>) {
        tipoEquipoList = newTipoEquipoList
        notifyDataSetChanged()
    }
}


