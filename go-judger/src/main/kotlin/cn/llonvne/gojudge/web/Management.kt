package cn.llonvne.gojudge.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.installManageWeb() {
    routing {
        get("/submit") {
            if (call.parameters.names().size != 1) {
                call.respond(HttpStatusCode.BadRequest)
            }
            val text = call.parameters["url"] ?: call.respond(HttpStatusCode.BadRequest, "must have url parameter!")

            call.respondHtml {
                body {
                    h1 {
                        +"OK $text"
                    }
                }
            }
        }

        get("/manage") {
            call.respondHtml {
                body {
                    h1 {
                        +"Judge Server Management"
                    }

                    form {
                        method = FormMethod.get
                        action = "/submit"

                        label {
                            +"Judge Url"
                        }
                        input {
                            type = InputType.text
                            name = "url"
                        }
                        input {
                            type = InputType.submit
                            value = "Submit"
                        }
                    }
                }
            }
        }
    }
}