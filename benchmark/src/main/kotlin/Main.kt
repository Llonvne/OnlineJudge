@file:OptIn(DelicateCoroutinesApi::class)

package cn.llonvne

import cn.llonvne.gojudge.api.LanguageDispatcher
import cn.llonvne.gojudge.api.LanguageFactory
import cn.llonvne.gojudge.api.SupportLanguages
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.benchmark.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis
import kotlin.time.Duration

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class JudgerBenchmark {
    private lateinit var languageDispatcher: LanguageDispatcher
    private val httpClient: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json)
        }
    }

    private fun default(judgeUrl: String): LanguageDispatcher {
        return LanguageDispatcher.get(
            LanguageFactory.get(
                judgeUrl, httpClient = httpClient
            )
        )
    }

    @Setup
    fun setUp() {
        languageDispatcher = default("http://localhost:8081/")
    }

    @Benchmark
    @Threads(20)
    fun cppHelloWorld() = runBlocking {
        languageDispatcher.dispatch(SupportLanguages.Cpp11) {
            judge(
                """
                #include <iostream>

                int main() {
                    std::cout << "Hello, World!" << std::endl;
                    return 0;
                }
            """.trimIndent(), ""
            )
        }
    }

    @Benchmark
    @Threads(20)
    fun kotlinHelloWorld() = runBlocking {
        languageDispatcher.dispatch(SupportLanguages.Kotlin) {
            judge("""println(""Hello, World!")""", "")
        }
    }
}

private lateinit var languageDispatcher: LanguageDispatcher
private val httpClient: HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json)
    }
}

private fun default(judgeUrl: String): LanguageDispatcher {
    return LanguageDispatcher.get(
        LanguageFactory.get(
            judgeUrl, httpClient = httpClient
        )
    )
}

suspend fun main() {
    languageDispatcher = default("http://localhost:8081/")

    listOf(10, 20, 50, 100, 200, 500, 1000).forEach { times ->
        measureTimeMillis {
            withContext(Dispatchers.IO) {
                (1..times).map {
                    async {
                        languageDispatcher.dispatch(SupportLanguages.Cpp11) {
                            judge(
                                """
                #include <iostream>

                int main() {
                    std::cout << "Hello, World!" << std::endl;
                    return 0;
                }
            """.trimIndent(), ""
                            )
                        }
                    }
                }.awaitAll()
            }
        }.also {
            println("$times $it")
        }
    }


}