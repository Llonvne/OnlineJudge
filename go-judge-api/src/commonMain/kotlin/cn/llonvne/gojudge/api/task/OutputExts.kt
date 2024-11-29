package cn.llonvne.gojudge.api.task

fun Output?.format(
    onNull: String,
    onNotNull: Output.() -> String,
): String =
    if (this == null) {
        onNull
    } else {
        onNotNull(this)
    }
