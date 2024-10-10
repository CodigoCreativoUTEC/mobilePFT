package com.codigocreativo.mobile.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.DetalleModeloFragment
import com.codigocreativo.mobile.objetos.Modelo
import com.codigocreativo.mobile.R

class ModeloAdapter(private var modelosList: MutableList<Modelo>, private val activity: FragmentActivity) : RecyclerView.Adapter<ModeloAdapter.ModeloViewHolder>() {

    class ModeloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val btnDetalle: Button = itemView.findViewById(R.id.btnDetalle)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modelo, parent, false)
        return ModeloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        val modelo = modelosList[position]
        holder.idTextView.text = modelo.id.toString()
        holder.nombreTextView.text = modelo.nombre
        holder.estadoTextView.text = modelo.estado.name

        // Acción del botón "Detalle"
        holder.btnDetalle.setOnClickListener {
            // Crear una instancia del fragmento de detalles
            val fragment = DetalleModeloFragment()

            // Pasar los datos del modelo seleccionado al fragmento mediante un bundle
            val bundle = Bundle()
            bundle.putInt("modelo_id", modelo.id)  // Pasar el ID del modelo
            fragment.arguments = bundle

            // Reemplazar el contenido actual con el fragmento de detalles
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)  // Usar el ID correcto del contenedor de fragmentos
                .addToBackStack(null)  // Agregar a la pila de retroceso para que pueda volver atrás
                .commit()
        }


        holder.btnEliminar.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro que desea eliminar el modelo ${modelo.nombre}?")
                .setPositiveButton("Aceptar") { _, _ ->
                    modelosList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, modelosList.size)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int {
        return modelosList.size
    }

    // Método para actualizar la lista filtrada
    fun updateList(newList: MutableList<Modelo>) {
        modelosList = newList
        notifyDataSetChanged()
    }
}

