package com.gamelauncher.core.domain.model

import java.util.UUID

enum class InstallationType {
    ANDROID_APK,
    ANDROID_APP_REFERENCE,
    LOCAL_FOLDER,
    LOCAL_WEB_GAME,
    ARCHIVE,
    EXTERNAL_RUNTIME,
    UNKNOWN
}

enum class Platform {
    ANDROID,
    WINDOWS,
    LINUX,
    MACOS,
    WEB,
    OTHER,
    UNKNOWN
}

enum class InstallationState {
    UNKNOWN,
    DISCOVERED,
    INSTALLED,
    MISSING,
    UNSUPPORTED,
    BROKEN,
    REMOVED
}

data class Installation(
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val type: InstallationType = InstallationType.UNKNOWN,
    val platform: Platform = Platform.UNKNOWN,
    val state: InstallationState = InstallationState.UNKNOWN,
    val launchAdapterId: String? = null,
    val runtimeId: String? = null,
    val path: String? = null,
    val uri: String? = null,
    val packageName: String? = null,
    val version: String? = null,
    val sizeBytes: Long? = null,
    val hash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
