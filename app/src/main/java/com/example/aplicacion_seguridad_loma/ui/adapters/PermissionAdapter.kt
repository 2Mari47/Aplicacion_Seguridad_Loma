package com.example.aplicacion_seguridad_loma.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_seguridad_loma.databinding.ItemPermissionBinding
import com.example.aplicacion_seguridad_loma.models.PermissionState
import com.example.aplicacion_seguridad_loma.models.PermissionType

class PermissionAdapter(
    private val onPermissionClick: (PermissionType) -> Unit
) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    private var permissions = mapOf<PermissionType, PermissionState>()

    fun updatePermissions(newPermissions: Map<PermissionType, PermissionState>) {
        permissions = newPermissions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val binding = ItemPermissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PermissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permissionType = PermissionType.values()[position]
        val state = permissions[permissionType] ?: PermissionState.NOT_REQUESTED
        holder.bind(permissionType, state, onPermissionClick)
    }

    override fun getItemCount(): Int = PermissionType.values().size

    class PermissionViewHolder(
        private val binding: ItemPermissionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            permissionType: PermissionType,
            state: PermissionState,
            onPermissionClick: (PermissionType) -> Unit
        ) {
            binding.apply {
                // Configurar información básica
                tvPermissionName.text = permissionType.displayName
                tvPermissionDescription.text = permissionType.description
                ivPermissionIcon.setImageResource(permissionType.iconRes)

                // Configurar estado visual
                when (state) {
                    PermissionState.GRANTED -> {
                        tvPermissionStatus.text = "✅ Concedido"
                        tvPermissionStatus.setTextColor(
                            itemView.context.getColor(android.R.color.holo_green_dark)
                        )
                        cardPermission.setCardBackgroundColor(
                            itemView.context.getColor(android.R.color.holo_green_light)
                        )
                        cardPermission.alpha = 1.0f
                    }
                    PermissionState.DENIED -> {
                        tvPermissionStatus.text = "❌ Denegado"
                        tvPermissionStatus.setTextColor(
                            itemView.context.getColor(android.R.color.holo_red_dark)
                        )
                        cardPermission.setCardBackgroundColor(
                            itemView.context.getColor(android.R.color.holo_red_light)
                        )
                        cardPermission.alpha = 0.8f
                    }
                    PermissionState.PERMANENTLY_DENIED -> {
                        tvPermissionStatus.text = "🚫 Denegado Permanentemente"
                        tvPermissionStatus.setTextColor(
                            itemView.context.getColor(android.R.color.darker_gray)
                        )
                        cardPermission.setCardBackgroundColor(
                            itemView.context.getColor(android.R.color.darker_gray)
                        )
                        cardPermission.alpha = 0.6f
                    }
                    PermissionState.NOT_REQUESTED -> {
                        tvPermissionStatus.text = "⏳ No Solicitado"
                        tvPermissionStatus.setTextColor(
                            itemView.context.getColor(android.R.color.holo_orange_dark)
                        )
                        cardPermission.setCardBackgroundColor(
                            itemView.context.getColor(android.R.color.white)
                        )
                        cardPermission.alpha = 1.0f
                    }
                }

                // Configurar click listener
                cardPermission.setOnClickListener {
                    onPermissionClick(permissionType)
                }

                // Animación sutil al hacer clic
                cardPermission.setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                        }
                    }
                    false
                }
            }
        }
    }
}