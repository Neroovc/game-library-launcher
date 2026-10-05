package com.gamelauncher.core.common

object GameLogger {
    fun d(category: LogCategory, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d("GameLauncher[${category.name}]", sanitize(message))
        }
    }

    fun i(category: LogCategory, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i("GameLauncher[${category.name}]", sanitize(message))
        }
    }

    fun w(category: LogCategory, message: String, throwable: Throwable? = null) {
        android.util.Log.w("GameLauncher[${category.name}]", sanitize(message), throwable)
    }

    fun e(category: LogCategory, message: String, throwable: Throwable? = null) {
        android.util.Log.e("GameLauncher[${category.name}]", sanitize(message), throwable)
    }

    private fun sanitize(message: String): String {
        // Remove potential sensitive data patterns
        return message
            .replace(Regex("token=[^&\\s]+", RegexOption.IGNORE_CASE), "token=***")
            .replace(Regex("password=[^&\\s]+", RegexOption.IGNORE_CASE), "password=***")
            .replace(Regex("auth=[^&\\s]+", RegexOption.IGNORE_CASE), "auth=***")
            .replace(Regex("cookie=[^&\\s]+", RegexOption.IGNORE_CASE), "cookie=***")
    }
}
