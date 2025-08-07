package com.saschl.sonygps.service

import android.util.Log
import timber.log.Timber

class FileTree : Timber.Tree() {
    companion object {
        private val logBuffer = mutableListOf<String>()
        fun getLogs(): List<String> = logBuffer.toList()
        fun clearLogs() = logBuffer.clear()
    }

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message. May be `null`, but then `t` will not be.
     * @param t Accompanying exceptions. May be `null`, but then `message` will not be.
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logEntry = "[${priorityToString(priority)}] ${tag ?: "App"}: $message" +
            (t?.let { "\n${it.stackTraceToString()}" } ?: "")
        logBuffer.add(logEntry)
        Log.i("FileTree", logEntry) // Also log to Android's Logcat for visibility
        if (logBuffer.size > 200) logBuffer.removeAt(0) // keep buffer size reasonable
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        android.util.Log.VERBOSE -> "V"
        android.util.Log.DEBUG -> "D"
        android.util.Log.INFO -> "I"
        android.util.Log.WARN -> "W"
        android.util.Log.ERROR -> "E"
        android.util.Log.ASSERT -> "A"
        else -> priority.toString()
    }
}