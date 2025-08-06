package com.example.aplicacion_seguridad_loma

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplicacion_seguridad_loma.R
import com.example.aplicacion_seguridad_loma.databinding.ActivityMainBinding
import com.example.aplicacion_seguridad_loma.models.PermissionState
import com.example.aplicacion_seguridad_loma.models.PermissionType
import com.example.aplicacion_seguridad_loma.ui.adapters.PermissionAdapter
import com.example.aplicacion_seguridad_loma.ui.adapters.PermissionLogAdapter
import com.example.aplicacion_seguridad_loma.viewmodel.MainViewModel
import com.example.aplicacion_seguridad_loma.ui.PrivacyPolicyActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var permissionAdapter: PermissionAdapter
    private lateinit var logAdapter: PermissionLogAdapter

    // Launcher para solicitar un solo permiso
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        handlePermissionResult(currentPermissionType, isGranted)
    }

    // Launcher para solicitar múltiples permisos
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        handlePermissionResult(currentPermissionType, allGranted)
    }

    private var currentPermissionType: PermissionType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si se aceptó la política de privacidad
        if (!isPrivacyPolicyAccepted()) {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
            finish()
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupUI()
        observeViewModel()
    }

    private fun isPrivacyPolicyAccepted(): Boolean {
        val sharedPref = getSharedPreferences("privacy_settings", MODE_PRIVATE)
        return sharedPref.getBoolean("privacy_policy_accepted", false)
    }

    private fun setupUI() {
        // Configurar RecyclerView de permisos
        permissionAdapter = PermissionAdapter { permissionType ->
            requestPermissionWithRationale(permissionType)
        }

        binding.rvPermissions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = permissionAdapter
        }

        // Configurar RecyclerView de logs
        logAdapter = PermissionLogAdapter()
        binding.rvLogs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = logAdapter
        }

        // Configurar botones
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshPermissionStates()
        }

        binding.btnClearLogs.setOnClickListener {
            showClearLogsConfirmation()
        }

        binding.btnSettings.setOnClickListener {
            openAppSettings()
        }
    }

    private fun observeViewModel() {
        // Observar estados de permisos
        viewModel.permissionStates.observe(this, Observer { states ->
            permissionAdapter.updatePermissions(states)
        })

        // Observar mensajes de estado
        viewModel.statusMessage.observe(this, Observer { message ->
            if (message.isNotEmpty()) {
                binding.tvStatus.text = message
                binding.tvStatus.visibility = android.view.View.VISIBLE
            } else {
                binding.tvStatus.visibility = android.view.View.GONE
            }
        })

        // Observar logs
        viewModel.permissionLogs.observe(this, Observer { logs ->
            logAdapter.submitList(logs)
            binding.tvLogsTitle.text = getString(R.string.logs_title) + " (${logs.size})"
        })
    }

    /**
     * Solicita un permiso mostrando primero la explicación si es necesario
     */
    private fun requestPermissionWithRationale(permissionType: PermissionType) {
        currentPermissionType = permissionType
        val permissions = getRelevantPermissions(permissionType)

        // Verificar si algún permiso necesita explicación
        val needsRationale = permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }

        if (needsRationale) {
            showPermissionRationale(permissionType) {
                requestPermissions(permissionType)
            }
        } else {
            requestPermissions(permissionType)
        }
    }

    /**
     * Muestra la explicación del permiso
     */
    private fun showPermissionRationale(permissionType: PermissionType, onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("🔒 Permiso Requerido")
            .setMessage("${permissionType.description}\n\n¿Desea conceder este permiso?")
            .setPositiveButton("Conceder") { _, _ ->
                onAccept()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                viewModel.logPermissionAction(permissionType, "CANCELADO", "Usuario canceló desde rationale")
            }
            .setIcon(permissionType.iconRes)
            .show()
    }

    /**
     * Solicita los permisos usando los launchers apropiados
     */
    private fun requestPermissions(permissionType: PermissionType) {
        val permissions = getRelevantPermissions(permissionType)

        viewModel.logPermissionAction(permissionType, "SOLICITADO")

        if (permissions.size == 1) {
            requestPermissionLauncher.launch(permissions[0])
        } else {
            requestMultiplePermissionsLauncher.launch(permissions)
        }
    }

    /**
     * Maneja la respuesta del usuario al permiso
     */
    private fun handlePermissionResult(permissionType: PermissionType?, granted: Boolean) {
        permissionType?.let {
            viewModel.updatePermissionState(it, granted)

            if (!granted) {
                // Verificar si fue denegado permanentemente
                val permissions = getRelevantPermissions(it)
                val permanentlyDenied = permissions.any { permission ->
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }

                if (permanentlyDenied) {
                    showPermanentlyDeniedDialog(it)
                }
            }
        }
        currentPermissionType = null
    }

    /**
     * Muestra diálogo cuando el permiso fue denegado permanentemente
     */
    private fun showPermanentlyDeniedDialog(permissionType: PermissionType) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Permiso Denegado")
            .setMessage("El permiso de ${permissionType.displayName} ha sido denegado permanentemente. Para habilitarlo, debe ir a Configuración > Aplicaciones > ${getString(R.string.app_name)} > Permisos.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Obtiene permisos relevantes según la versión de Android
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
     * Abre la configuración de la aplicación
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * Muestra confirmación para limpiar logs
     */
    private fun showClearLogsConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Limpiar Historial")
            .setMessage("¿Está seguro de que desea eliminar todo el historial de actividad?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.clearAllLogs()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar estados cuando se regrese de configuración
        viewModel.refreshPermissionStates()
    }
}