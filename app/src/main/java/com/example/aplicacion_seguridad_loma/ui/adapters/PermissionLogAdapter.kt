package com.example.aplicacion_seguridad_loma.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_seguridad_loma.databinding.ItemPermissionLogBinding
import com.example.aplicacion_seguridad_loma.models.PermissionLog
import java.text.SimpleDateFormat
import java.util.*

class PermissionLogAdapter : ListAdapter<PermissionLog, PermissionLogAdapter.LogViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemPermissionLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(
        private val binding: ItemPermissionLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        fun bind(log: PermissionLog) {
            binding.apply {
                tvLogPermission.text = log.permissionType
                tvLogAction.text = log.action
                tvLogTimestamp.text = dateFormat.format(log.timestamp)
                tvLogDetails.text = log.details

                // Configurar color según la acción
                val actionColor = when (log.action) {
                    "CONCEDIDO" -> android.R.color.holo_green_dark
                    "DENEGADO" -> android.R.color.holo_red_dark
                    "SOLICITADO" -> android.R.color.holo_orange_dark
                    "CANCELADO" -> android.R.color.darker_gray
                    else -> android.R.color.black
                }

                tvLogAction.setTextColor(itemView.context.getColor(actionColor))

                // Mostrar/ocultar detalles si existen
                if (log.details.isNotEmpty()) {
                    tvLogDetails.visibility = android.view.View.VISIBLE
                } else {
                    tvLogDetails.visibility = android.view.View.GONE
                }
            }
        }
    }

    private class LogDiffCallback : DiffUtil.ItemCallback<PermissionLog>() {
        override fun areItemsTheSame(oldItem: PermissionLog, newItem: PermissionLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PermissionLog, newItem: PermissionLog): Boolean {
            return oldItem == newItem
        }
    }
}