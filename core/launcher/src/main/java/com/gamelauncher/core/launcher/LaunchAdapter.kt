package com.gamelauncher.core.launcher

interface LaunchAdapter {
    fun canLaunch(installation: Installation): Boolean
    fun launch(installation: Installation): LaunchResult
}

data class Installation(
    val id: String,
    val name: String,
    val type: String = "UNKNOWN",
    val packageName: String? = null,
    val path: String? = null,
    val uri: String? = null
)

sealed class LaunchResult {
    object Success : LaunchResult()
    object InstallationMissing : LaunchResult()
    object RuntimeMissing : LaunchResult()
    object UnsupportedPlatform : LaunchResult()
    object PackageNotInstalled : LaunchResult()
    object PermissionDenied : LaunchResult()
    object ActivityNotFound : LaunchResult()
    object ExternalRuntimeError : LaunchResult()
    object UnknownError : LaunchResult()
}
