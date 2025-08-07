package com.saschl.sonygps.service

import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log errors and warnings to avoid spam
        if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
            // You could also send to crashlytics or other crash reporting service here
            super.log(priority, tag, message, t)
        }
    }
}

class GlobalExceptionHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            // Log the uncaught exception with Timber
            Timber.e(exception, "Uncaught exception in thread ${thread.name}")

            // Also log some additional context
            Timber.e("Thread: ${thread.name}, ID: ${thread.threadId()}")
            Timber.e( "Exception: ${exception.javaClass.simpleName}")
            Timber.e( "Message: ${exception.message}")
            Timber.e( "Stack trace: ${exception.stackTraceToString()}")

        } catch (e: Exception) {
            // If logging fails for some reason, we don't want to cause another crash
            e.printStackTrace()
        } finally {
            // Call the default handler to let the system handle the crash normally
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
