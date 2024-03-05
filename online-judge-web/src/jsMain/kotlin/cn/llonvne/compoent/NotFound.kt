package cn.llonvne.compoent

import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

interface NotFoundAble {
    val header: String
    val notice: String
    val errorCode: String
}

fun Container.notFound(header: String, notice: String, errorCode: String) {
    div(className = "p-2") {
        div(className = "alert alert-danger") {
            setAttribute("role", "alert")
            h1 {
                +header
            }

            p(className = "p-1") {
                +notice
            }

            div {
                +"ErrorCode:${errorCode}"
            }
        }
    }
}

fun <T : NotFoundAble> Container.notFound(notFoundAble: T) {
    div(className = "p-2") {
        div(className = "alert alert-danger") {
            setAttribute("role", "alert")
            h1 {
                +notFoundAble.header
            }

            p(className = "p-1") {
                +notFoundAble.notice
            }

            div {
                +"ErrorCode:${notFoundAble.errorCode}"
            }
        }
    }
}

