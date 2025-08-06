package com.example.aplicacion_seguridad_loma.models

enum class PermissionType(
    val displayName: String,
    val description: String,
    val iconRes: Int,
    val permissions: Array<String>
) {
    CAMERA(
        "Cámara",
        "Necesitamos acceso a la cámara para tomar fotos y videos dentro de la aplicación",
        android.R.drawable.ic_menu_camera,
        arrayOf(android.Manifest.permission.CAMERA)
    ),
    GALLERY(
        "Galería",
        "Acceso a la galería para seleccionar y mostrar imágenes y videos",
        android.R.drawable.ic_menu_gallery,
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ),
    MICROPHONE(
        "Micrófono",
        "Acceso al micrófono para grabación de audio y llamadas",
        android.R.drawable.ic_btn_speak_now,
        arrayOf(android.Manifest.permission.RECORD_AUDIO)
    ),
    PHONE(
        "Teléfono",
        "Permiso para realizar llamadas telefónicas directamente desde la app",
        android.R.drawable.sym_action_call,
        arrayOf(android.Manifest.permission.CALL_PHONE)
    ),
    CONTACTS(
        "Contactos",
        "Acceso a contactos para facilitar la comunicación con otros usuarios",
        android.R.drawable.sym_action_email,
        arrayOf(android.Manifest.permission.READ_CONTACTS)
    ),
    LOCATION(
        "Ubicación",
        "Acceso a la ubicación para servicios basados en localización",
        android.R.drawable.ic_menu_mylocation,
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}