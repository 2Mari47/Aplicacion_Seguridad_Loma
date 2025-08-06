package com.example.aplicacion_seguridad_loma.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.aplicacion_seguridad_loma.models.PermissionLog

@Dao
interface PermissionLogDao {
    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<PermissionLog>>

    @Insert
    suspend fun insertLog(log: PermissionLog)

    @Query("DELETE FROM permission_logs")
    suspend fun clearLogs()
}