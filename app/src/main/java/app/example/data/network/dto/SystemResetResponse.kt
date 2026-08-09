package app.example.data.network.dto

import kotlinx.serialization.Serializable

/**
 * API response wrapper for the system reset endpoint (POST /api/system/reset).
 */
@Serializable
data class SystemResetResponse(
    val success: Boolean,
    val data: ResetData? = null,
)

/** Detail breakdown of re-seeded demo emails after reset. */
@Serializable
data class ResetData(
    val message: String? = null,
    val timestamp: Long? = null,
    val emailsSeeded: Int? = null,
    val inbox: Int? = null,
    val drafts: Int? = null,
    val sent: Int? = null,
)
