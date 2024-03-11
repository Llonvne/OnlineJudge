package cn.llonvne.gojudge.api

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import io.ktor.client.*
import kotlinx.serialization.Serializable

interface JudgeServerApi {
    @Headers("Content-Type:Application/Json")
    @GET("/info")
    suspend fun info(): JudgeServerInfo

    companion object {
        fun get(baseUrl: String, httpClient: HttpClient): JudgeServerApi {
            return object : JudgeServerApi {

                private val ktorfit = Ktorfit.Builder()
                    .baseUrl(baseUrl)
                    .httpClient(httpClient)
                    .build()

                val api = ktorfit.create<JudgeServerApi>()

                override suspend fun info(): JudgeServerInfo {
                    return api.info()
                }
            }
        }
    }
}

@Serializable
data class JudgeServerInfo(
    val name: String,
    val host: String,
    val port: String,
    val cpuCoresCount: Int,
    val cpuUsage: Double,
    val memoryUsage: Int,
    val isOnline: Boolean
)