package cn.llonvne.gojudge.app

import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.Test

class JugderKtTest {

    @Test
    fun testGetVersion2() = testApplication {
        application {
            judging {

            }
        }
        client.post("/version") {
            setBody("Hello From Test")
        }
    }
}