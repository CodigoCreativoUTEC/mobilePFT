package com.codigocreativo.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class MarcaAdapter(private var marcasList: MutableList<Marca>, private val activity: FragmentActivity) : RecyclerView.Adapter<MarcaAdapter.MarcaViewHolder>() {

    class MarcaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
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

        // Acción del botón "Detalle"
        holder.btnDetalle.setOnClickListener {
            // Crear una instancia del fragmento y pasarle los datos de la marca
            val fragment = DetalleMarcaFragment()
            val bundle = Bundle()
            bundle.putInt("marca_id", marca.id)
            fragment.arguments = bundle

            // Reemplazar el contenido actual con el fragmento de detalles
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        holder.btnEliminar.setOnClickListener {
            // Crear el diálogo de confirmación para eliminar
            AlertDialog.Builder(activity)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro que desea eliminar la marca ${marca.nombre}?")
                .setPositiveButton("Aceptar") { _, _ ->
                    // Simulación de eliminación
                    marcasList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, marcasList.size)
                }
                .setNegativeButton("Cancelar", null)  // No hacer nada si se cancela
                .show()
        }

    }

    override fun getItemCount(): Int {
        return marcasList.size
    }

    // Método para actualizar la lista filtrada
    fun updateList(newList: MutableList<Marca>) {
        marcasList = newList
        notifyDataSetChanged()
    }


}


