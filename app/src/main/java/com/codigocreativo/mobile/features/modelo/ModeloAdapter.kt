package com.codigocreativo.mobile.features.modelo


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R


class ModeloAdapter(
    var modelosList: List<Modelo>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Modelo) -> Unit
) : RecyclerView.Adapter<ModeloAdapter.ModeloViewHolder>() {

    inner class ModeloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nombreTextView: TextView = itemView.findViewById(R.id.nombreModeloTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoModeloTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modelo, parent, false)
        return ModeloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        val modelo = modelosList[position]
        holder.nombreTextView.text = modelo.nombre
        holder.estadoTextView.text = modelo.estado.name

        // Manejar el clic en el elemento completo
        holder.itemView.setOnClickListener {
            onDetalleClick(modelo)
        }
    }

    override fun getItemCount(): Int {
        return modelosList.size
    }

    fun updateList(newModeloList: List<Modelo>) {
        modelosList = newModeloList
        notifyDataSetChanged()
    }
}



