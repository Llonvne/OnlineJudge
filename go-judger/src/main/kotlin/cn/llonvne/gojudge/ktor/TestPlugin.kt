package cn.llonvne.gojudge.ktor

import at.kopyk.CopyExtensions
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@CopyExtensions
@Serializable
data class User(val username: String, val password: String)

val HiddenUserPassword = createApplicationPlugin(name = "HiddenUserPassword") {
    onCallRespond { call ->
        transformBody { body: Any ->
            if (body is User){
                body.copy(password = "")
            }
            else{
                body
            }
        }
    }
}