package cn.llonvne

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 获得 Logger 实例，默认使用 [LoggerFactory.getLogger] 构建
 */
inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

/**
 * 将 [value] 的 hashcode 作为标识符，执行 [action]
 */
@OptIn(ExperimentalContracts::class)
suspend fun <T, R> Logger.track(
    vararg value: T,
    action: suspend Logger.() -> R,
): R {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return TrackedLogger(this, value.toList()).action()
}

/**
 * 带有标识符号(Hashcode)的Logger
 */
class TrackedLogger<T>(
    private val logger: Logger,
    private val value: T,
) : Logger by logger {
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
