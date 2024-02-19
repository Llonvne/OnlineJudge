package cn.llonvne

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

fun <T> Logger.track(vararg value: T): Logger {
    return TrackedLogger(this, value.toList())
}

suspend fun <T, R> Logger.track(vararg value: T, action: suspend Logger.() -> R): R {
    return TrackedLogger(this, value.toList()).action()
}


class TrackedLogger<T>(private val logger: Logger, private val value: T) : Logger by logger {
    override fun info(msg: String?) {
        logger.info("[id-${value.hashCode()}] $msg")
    }

    override fun warn(msg: String?) {
        logger.warn("[id-${value.hashCode()}] $msg")
    }

    override fun error(msg: String?) {
        logger.error("[id-${value.hashCode()}] $msg")
    }

    override fun debug(msg: String?) {
        logger.debug("[id-${value.hashCode()}] $msg")
    }
}