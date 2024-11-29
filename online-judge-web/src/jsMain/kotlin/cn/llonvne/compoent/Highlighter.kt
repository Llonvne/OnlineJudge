package cn.llonvne.compoent

import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.code
import io.kvision.html.customTag
import io.kvision.html.div

fun Container.codeHighlighter(
    code: String,
    init: Div.() -> Unit = {},
) = div {
    customTag("pre") {
        code(code) {
        }
    }.addAfterInsertHook {
        js("hljs.highlightAll()")
        Unit
    }

    init()
}
