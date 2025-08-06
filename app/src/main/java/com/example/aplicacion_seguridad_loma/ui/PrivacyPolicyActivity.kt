package com.example.aplicacion_seguridad_loma.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplicacion_seguridad_loma.R
import com.example.aplicacion_seguridad_loma.databinding.ActivityPrivacyPolicyBinding
import com.example.aplicacion_seguridad_loma.MainActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)

        setupClickListeners()
        loadPrivacyPolicy()
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            // Guardar aceptación de política de privacidad
            savePrivacyPolicyAcceptance()

            // Navegar a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnDecline.setOnClickListener {
            // Si el usuario rechaza, cerrar la aplicación
            finish()
        }
    }

    private fun loadPrivacyPolicy() {
        binding.tvPrivacyPolicy.text = getString(R.string.privacy_policy_text)
    }

    private fun savePrivacyPolicyAcceptance() {
        val sharedPref = getSharedPreferences("privacy_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("privacy_policy_accepted", true)
            putLong("acceptance_timestamp", System.currentTimeMillis())
            apply()
        }
    }
}