package cn.llonvne

import cn.llonvne.gojudge.judging
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty) {
        judging {}
    }.start(wait = true)
}