package com.example.aplicacion_seguridad_loma.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.aplicacion_seguridad_loma.database.AppDatabase
import com.example.aplicacion_seguridad_loma.models.PermissionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val permissionLogDao = database.permissionLogDao()

    fun getAllLogs(): LiveData<List<PermissionLog>> {
        return permissionLogDao.getAllLogs()
    }

    suspend fun insertLog(log: PermissionLog) {
        withContext(Dispatchers.IO) {
            permissionLogDao.insertLog(log)
        }
    }

    suspend fun clearLogs() {
        withContext(Dispatchers.IO) {
            permissionLogDao.clearLogs()
        }
    }
}