package com.codigocreativo.mobile.features.marca


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R


class MarcaAdapter(
    var marcaList: List<Marca>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Marca) -> Unit
) : RecyclerView.Adapter<MarcaAdapter.MarcaViewHolder>() {

    inner class MarcaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nombreTextView: TextView = itemView.findViewById(R.id.nombreMarcaTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoMarcaTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarcaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marca, parent, false)
        return MarcaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarcaViewHolder, position: Int) {
        val marca = marcaList[position]
        holder.nombreTextView.text = marca.nombre
        holder.estadoTextView.text = marca.estado.name

        // Manejar el clic en el elemento completo
        holder.itemView.setOnClickListener {
            onDetalleClick(marca)
        }
    }

    override fun getItemCount(): Int {
        return marcaList.size
    }

    fun updateList(newMarcaList: List<Marca>) {
        marcaList = newMarcaList
        notifyDataSetChanged()
    }
}

