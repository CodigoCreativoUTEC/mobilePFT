package com.codigocreativo.mobile.features.usuarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.codigocreativo.mobile.R

class UsuariosAdapter(
    var usuarioList: List<Usuario>,
    private val activity: FragmentActivity,
    private val onDetalleClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuariosViewHolder>() {

    inner class UsuariosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuariosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuariosViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuariosViewHolder, position: Int) {
        val usuario = usuarioList[position]

        holder.nombreTextView.text = usuario.nombre
        holder.estadoTextView.text = usuario.estado.name

        holder.itemView.setOnClickListener {
            onDetalleClick(usuario)
        }
    }

    override fun getItemCount(): Int {
        return usuarioList.size
    }

    fun updateList(newUsuarioList: List<Usuario>) {
        usuarioList = newUsuarioList
        notifyDataSetChanged()
    }
}