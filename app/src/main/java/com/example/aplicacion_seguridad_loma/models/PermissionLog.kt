package com.example.aplicacion_seguridad_loma.models
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "permission_logs")
data class PermissionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val permissionType: String,
    val action: String, // "GRANTED", "DENIED", "REQUESTED"
    val timestamp: Date = Date(),
    val details: String = ""
)