package com.gamelauncher.core.common

sealed class NetworkError {
    object Timeout : NetworkError()
    object NoConnection : NetworkError()
    object Unknown : NetworkError()
}
