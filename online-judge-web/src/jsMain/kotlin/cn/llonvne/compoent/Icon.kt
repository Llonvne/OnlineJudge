package cn.llonvne.compoent

import io.kvision.core.Container
import io.kvision.html.I
import io.kvision.html.i

private fun fa(name: String) = "fa-$name"

private fun fas(vararg names: String) =
    names
        .map {
            fa(it)
        }.joinToString(" ")

private const val SOLID = "solid"

private const val SHAKE = "shake"

private fun Container.faIcon(
    vararg names: String,
    init: I.() -> Unit,
) {
    i(className = fas(*names)) {
        init()
    }
}

fun Container.circleCheck(init: I.() -> Unit = {}) {
    faIcon(SOLID, "circle-check", init = init)
}
