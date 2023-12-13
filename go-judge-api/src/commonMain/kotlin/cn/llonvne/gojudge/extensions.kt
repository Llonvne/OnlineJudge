package cn.llonvne.gojudge

fun <T : Any, R> T?.map(transform: (T) -> R?): R? = this?.let(transform)