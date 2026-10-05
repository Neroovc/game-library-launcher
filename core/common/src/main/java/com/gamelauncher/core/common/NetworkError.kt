package com.gamelauncher.core.common

sealed class NetworkError {
    object Timeout : NetworkError()
    object NoConnection : NetworkError()
    object RateLimit : NetworkError()
    object Unauthorized : NetworkError()
    data class Http(val code: Int, val message: String) : NetworkError()
    data class Parse(val message: String) : NetworkError()
    object Unknown : NetworkError()
}
