package com.example.model

enum class RestrictionType(val displayName: String, val description: String) {
    FREE(
        "Libre (Sin Restricción)",
        "Perfil público. Cualquier persona y dispositivo que tenga la app puede importar y conectarse libremente sin límites."
    ),
    EXPIRATION(
        "Bloqueo por Fecha y Hora (Prueba / Expiración)",
        "Permite definir la fecha y hora exacta de vencimiento (30 min, 1 hora, días o calendario). Al vencer el tiempo, la app bloquea automáticamente la conexión."
    ),
    SINGLE_ID(
        "HWID Único (Dispositivo Fijo)",
        "El archivo .dtp solo funcionará en el dispositivo con el HWID que especifiques aquí (grabado dentro del archivo)."
    ),
    MULTI_ID(
        "Lista Fija de HWIDs (Varios Dispositivos)",
        "El archivo .dtp solo funcionará en los dispositivos cuyos HWIDs estén incluidos en esta lista (grabados dentro del archivo)."
    ),
    VPS_HWID_CHECK(
        "Validar HWIDs en tu VPS (Un solo archivo para todos)",
        "Creas un único archivo .dtp para todos tus usuarios. Al conectar, la app consulta tu VPS en tiempo real. Cuando un cliente te pague, solo agregas su HWID en tu VPS sin reenviar archivos."
    )
}

data class DtpAccessRules(
    val restrictionType: RestrictionType = RestrictionType.FREE,
    val expirationTimestamp: Long? = null,
    val allowedDeviceId: String? = null,
    val allowedDeviceIds: List<String> = emptyList(),
    val remoteValidationUrl: String? = null,
    val isConfigLocked: Boolean = false,
    val creatorNote: String = "",
    val exportTimestamp: Long = System.currentTimeMillis()
)

data class DtpProfilePackage(
    val version: Int = 1,
    val profileName: String,
    val sshConfig: SshConfig,
    val accessRules: DtpAccessRules
)

data class DtpValidationResult(
    val isValid: Boolean,
    val title: String = "",
    val message: String = "",
    val details: String? = null
)
