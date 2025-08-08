package com.saschl.sonygps.service

import android.content.Context
import timber.log.Timber
import com.saschl.sonygps.database.LogRepository
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileTree(context: Context) : Timber.Tree() {
    private val logRepository = LogRepository(context.applicationContext)

    companion object {
        @Volatile
        private var logRepository: LogRepository? = null
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        fun initialize(context: Context) {
            if (logRepository == null) {
                synchronized(this) {
                    if (logRepository == null) {
                        logRepository = LogRepository(context.applicationContext)
                    }
                }
            }
        }

        fun getLogs(): List<String> {
            return logRepository?.let { repo ->
                runBlocking {
                    repo.getRecentLogs().map { logEntry ->
                        val date = dateFormat.format(Date(logEntry.timestamp))
                        "[$date] [${priorityToString(logEntry.priority)}] ${logEntry.tag ?: "App"}: ${logEntry.message}" +
                                (logEntry.exception?.let { "\n$it" } ?: "")
                    }
                }
            } ?: emptyList()
        }

        suspend fun clearLogs() {
            logRepository?.clearAllLogs()
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

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message. May be `null`, but then `t` will not be.
     * @param t Accompanying exceptions. May be `null`, but then `message` will not be.
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = System.currentTimeMillis()
        val exception = t?.stackTraceToString()

        logRepository.insertLog(timestamp, priority, tag, message, exception)
    }
}