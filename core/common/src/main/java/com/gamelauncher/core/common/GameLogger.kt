package com.gamelauncher.core.common

object GameLogger {
    fun d(category: LogCategory, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d("GameLauncher[${category.name}]", message)
        }
    }

    fun i(category: LogCategory, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i("GameLauncher[${category.name}]", message)
        }
    }

    fun w(category: LogCategory, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w("GameLauncher[${category.name}]", message)
        } else {
            android.util.Log.w("GameLauncher[${category.name}]", sanitize(message))
        }
    }

    fun e(category: LogCategory, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e("GameLauncher[${category.name}]", message, throwable)
        } else {
            android.util.Log.e("GameLauncher[${category.name}]", sanitize(message), null)
        }
    }

    private fun sanitize(message: String): String {
        return message
    }
}
