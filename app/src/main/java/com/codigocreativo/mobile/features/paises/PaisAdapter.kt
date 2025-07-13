package com.codigocreativo.mobile.features.paises

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R

class PaisAdapter(
    var paisList: List<Pais>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Pais) -> Unit
) : RecyclerView.Adapter<PaisAdapter.PaisViewHolder>() {

    inner class PaisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.nombrePaisTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoPaisTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaisViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pais, parent, false)
        return PaisViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaisViewHolder, position: Int) {
        val pais = paisList[position]
        holder.nombreTextView.text = pais.nombre
        holder.estadoTextView.text = pais.estado.name

        // Manejar el clic en el elemento completo
        holder.itemView.setOnClickListener {
            onDetalleClick(pais)
        }
    }

    override fun getItemCount(): Int {
        return paisList.size
    }

    fun updateList(newPaisList: List<Pais>) {
        paisList = newPaisList
        notifyDataSetChanged()
    }
} 