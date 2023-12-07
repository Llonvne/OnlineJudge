package cn.llonvne

import cn.llonvne.gojudge.app.judging
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 3000) {
        judging {}
    }.start(wait = true)
}