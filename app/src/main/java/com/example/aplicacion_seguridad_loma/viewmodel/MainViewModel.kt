package com.example.aplicacion_seguridad_loma.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_seguridad_loma.models.PermissionLog
import com.example.aplicacion_seguridad_loma.models.PermissionState
import com.example.aplicacion_seguridad_loma.models.PermissionType
import com.example.aplicacion_seguridad_loma.repository.PermissionRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PermissionRepository(application)
    private val context: Context = application.applicationContext

    // LiveData para el estado de los permisos
    private val _permissionStates = MutableLiveData<Map<PermissionType, PermissionState>>()
    val permissionStates: LiveData<Map<PermissionType, PermissionState>> = _permissionStates

    // LiveData para mensajes de estado
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    // LiveData para logs
    val permissionLogs: LiveData<List<PermissionLog>> = repository.getAllLogs()

    init {
        refreshPermissionStates()
    }

    /**
     * Actualiza el estado de todos los permisos
     */
    fun refreshPermissionStates() {
        val states = mutableMapOf<PermissionType, PermissionState>()

        PermissionType.values().forEach { permissionType ->
            states[permissionType] = getPermissionState(permissionType)
        }

        _permissionStates.value = states
    }

    /**
     * Obtiene el estado actual de un permiso específico
     */
    private fun getPermissionState(permissionType: PermissionType): PermissionState {
        val permissions = getRelevantPermissions(permissionType)

        return when {
            permissions.all { isPermissionGranted(it) } -> PermissionState.GRANTED
            permissions.any { isPermissionGranted(it) } -> PermissionState.GRANTED
            else -> PermissionState.DENIED
        }
    }

    /**
     * Filtra permisos relevantes según la versión de Android
     */
    private fun getRelevantPermissions(permissionType: PermissionType): Array<String> {
        return when (permissionType) {
            PermissionType.GALLERY -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                } else {
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> permissionType.permissions
        }
    }

    /**
     * Verifica si un permiso específico está concedido
     */
    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Registra una acción de permiso en la base de datos
     */
    fun logPermissionAction(permissionType: PermissionType, action: String, details: String = "") {
        viewModelScope.launch {
            val log = PermissionLog(
                permissionType = permissionType.displayName,
                action = action,
                details = details
            )
            repository.insertLog(log)
        }
    }

    /**
     * Actualiza el estado de un permiso específico después de la respuesta del usuario
     */
    fun updatePermissionState(permissionType: PermissionType, granted: Boolean) {
        val currentStates = _permissionStates.value?.toMutableMap() ?: mutableMapOf()
        currentStates[permissionType] = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        _permissionStates.value = currentStates

        val action = if (granted) "CONCEDIDO" else "DENEGADO"
        val message = if (granted) {
            "✅ Permiso de ${permissionType.displayName} concedido correctamente"
        } else {
            "❌ Permiso de ${permissionType.displayName} denegado"
        }

        _statusMessage.value = message
        logPermissionAction(permissionType, action)

        // Limpiar el mensaje después de 3 segundos
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _statusMessage.value = ""
        }
    }

    /**
     * Limpia todos los logs
     */
    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _statusMessage.value = "📝 Historial de permisos limpiado"
            kotlinx.coroutines.delay(2000)
            _statusMessage.value = ""
        }
    }
}