package com.saschl.sonygps.database

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LogRepository(context: Context) {
    private val logDao = LogDatabase.getDatabase(context).logDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun insertLog(timestamp: Long, priority: Int, tag: String?, message: String, exception: String?) {
        scope.launch {
            val logEntry = LogEntry(
                timestamp = timestamp,
                priority = priority,
                tag = tag,
                message = message,
                exception = exception
            )
            logDao.insertLog(logEntry)

            // Clean up old logs to prevent database from growing too large
            val count = logDao.getLogCount()
            if (count > 1000) {
                logDao.deleteOldLogs(500) // Keep only latest 500 logs
            }
        }
    }

    fun getAllLogs(): Flow<List<LogEntry>> = logDao.getAllLogs()

    suspend fun getRecentLogs(limit: Int = 200): List<LogEntry> = logDao.getRecentLogs(limit)

    suspend fun clearAllLogs() {
        logDao.clearAllLogs()
    }
}
