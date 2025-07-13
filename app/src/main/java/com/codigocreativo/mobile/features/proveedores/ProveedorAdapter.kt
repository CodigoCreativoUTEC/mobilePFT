package com.codigocreativo.mobile.features.proveedores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val nombreTextView: TextView = itemView.findViewById(R.id.nombreProveedorTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoProveedorTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_proveedor, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val proveedor = proveedorList[position]

        holder.nombreTextView.text = proveedor.nombre
        holder.estadoTextView.text = proveedor.estado.name

        holder.itemView.setOnClickListener {
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