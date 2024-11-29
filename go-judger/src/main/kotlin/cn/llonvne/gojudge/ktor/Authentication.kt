package cn.llonvne.gojudge.ktor

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

const val GLOBAL_AUTHENTICATION = "auth-bearer"

fun Route.globalAuth(build: Route.() -> Unit) = authenticate(GLOBAL_AUTHENTICATION, build = build)

fun Application.installAuthentication() {
    authentication {
        bearer(GLOBAL_AUTHENTICATION) {
            realm = "Access to the '/' path"

            skipWhen {
                it.request.queryParameters["dev"] == "true"
            }

            authenticate { tokenCredential ->
                if (tokenCredential.token == "abc123") {
                    return@authenticate UserIdPrincipal("jetbrains")
                }
                null
            }
        }
    }
}
