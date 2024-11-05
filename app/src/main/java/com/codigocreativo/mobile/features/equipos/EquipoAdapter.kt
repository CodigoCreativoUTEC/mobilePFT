package com.codigocreativo.mobile.features.equipos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codigocreativo.mobile.R

class EquipoAdapter(
    var equipoList: List<Equipo>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Equipo) -> Unit
) : RecyclerView.Adapter<EquipoAdapter.EquipoViewHolder>() {

    inner class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_equipo, parent, false)
        return EquipoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val equipo = equipoList[position]

        // Cargar imagenes con Glide
        Glide.with(activity)
            .load(equipo.imagen)
            .centerCrop()
            .into(holder.imageView)
        holder.nombreTextView.text = equipo.nombre
        holder.estadoTextView.text = equipo.estado.name

        holder.btnDetalle.setOnClickListener {
            onDetalleClick(equipo)
        }
    }

    override fun getItemCount(): Int {
        return equipoList.size
    }

    fun updateList(newEquiposList: List<Equipo>) {
        equipoList = newEquiposList
        notifyDataSetChanged()
    }
}