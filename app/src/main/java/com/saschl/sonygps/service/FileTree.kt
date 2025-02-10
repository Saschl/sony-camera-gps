package com.saschl.sonygps.service

import timber.log.Timber

class FileTree : Timber.Tree() {

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message. May be `null`, but then `t` will not be.
     * @param t Accompanying exceptions. May be `null`, but then `message` will not be.
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        /* if (!Fabric.isInitialized()) {
             return
         }

         Crashlytics.log(priority, tag, message)*/
    }
}