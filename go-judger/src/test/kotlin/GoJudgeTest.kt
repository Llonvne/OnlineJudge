import cn.llonvne.gojudge.api.getVersion
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GoJudgeTest {
    @Test
    fun test() = runBlocking {
        val httpClient = HttpClient {
            install(Resources)
            defaultRequest {
                host = "0.0.0.0"
                port = 3000
                url { protocol = URLProtocol.HTTP }
            }
        };

    }
}