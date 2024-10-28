package com.codigocreativo.mobile.features.proveedores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R

class ProveedorAdapter(
    var proveedorList: List<Proveedor>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Proveedor) -> Unit
) : RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>() {

    inner class ProveedorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_proveedor, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val proveedor = proveedorList[position]
        holder.idTextView.text = proveedor.idProveedor.toString()
        holder.nombreTextView.text = proveedor.nombre
        holder.estadoTextView.text = proveedor.estado.name

        holder.btnDetalle.setOnClickListener {
            onDetalleClick(proveedor)
        }
    }

    override fun getItemCount(): Int {
        return proveedorList.size
    }

    fun updateList(newProveedorsList: List<Proveedor>) {
        proveedorList = newProveedorsList
        notifyDataSetChanged()
    }
}