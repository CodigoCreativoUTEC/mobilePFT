package com.codigocreativo.mobile.features.proveedores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R

class ProveedorAdapter(
    private var proveedorList: List<Proveedor>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>() {

    inner class ProveedorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modelo, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val modelo = proveedorList[position]
        holder.idTextView.text = modelo.idProveedor.toString()
        holder.nombreTextView.text = modelo.nombre
        holder.estadoTextView.text = modelo.estado.name

        holder.btnDetalle.setOnClickListener {
            val fragment = DetalleProveedorFragment().apply {
                arguments = Bundle().apply {
                    putInt("id", modelo.idProveedor)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return proveedorList.size
    }

    // Método para actualizar la lista de modelos y notificar al adaptador de los cambios
    fun updateList(newProveedorsList: List<Proveedor>) {
        proveedorList = newProveedorsList
        notifyDataSetChanged()
    }
}


