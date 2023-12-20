package cn.llonvne.gojudge

/**
 * 类似于 Rust Option 的 map
 * 用于 A? -> B?
 * ```kotlin
 * val a:Int? = 1 // a 为 Int?
 * val b = a.map { it.toString() } // b 为 String?
 * ```
 */
fun <T : Any, R> T?.map(transform: (T) -> R?): R? = this?.let(transform)
