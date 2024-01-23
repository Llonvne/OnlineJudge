package cn.llonvne.gojudge.api

import cn.llonvne.gojudge.api.task.Output
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.*
import io.ktor.client.*
import io.ktor.serialization.*

internal interface LanguageRouterKtorfitInternalApi {
    @Headers("Content-Type:Application/Json")
    @GET("{language}/version")
    suspend fun version(
        @Path language: String
    ): String

    @Headers("Content-Type:Application/Json")
    @POST("{language}")
    @FormUrlEncoded
    suspend fun judge(
        @Path language: String,
        @Field("code") code: String, @Field("stdin") stdin: String,
    ): Output
}

interface LanguageRouterKtorfitApi {
    suspend fun version(): String
    suspend fun judge(
        code: String, stdin: String,
    ): Output
}

interface LanguageFactory {
    fun getLanguageApi(language: SupportLanguages): LanguageRouterKtorfitApi

    companion object {
        fun get(baseUrl: String, httpClient: HttpClient): LanguageFactory {
            return object : LanguageFactory {

                private val ktorfit = Ktorfit.Builder()
                    .baseUrl(baseUrl)
                    .httpClient(httpClient)
                    .build()

                override fun getLanguageApi(language: SupportLanguages): LanguageRouterKtorfitApi {
                    return object : LanguageRouterKtorfitApi {

                        private val service = ktorfit.create<LanguageRouterKtorfitInternalApi>()

                        override suspend fun version(): String {
                            return service.version(language.path)
                        }

                        override suspend fun judge(code: String, stdin: String): Output {
                            return service.judge(language.path, code, stdin)
                        }
                    }
                }
            }
        }
    }
}

interface LanguageDispatcher {
    suspend fun <R> dispatch(language: SupportLanguages, on: suspend LanguageRouterKtorfitApi.() -> R): R

    companion object {
        fun get(languageFactory: LanguageFactory): LanguageDispatcher = LanguageDispatcherImpl(languageFactory)
    }
}

private class LanguageDispatcherImpl(
    private val languageFactory: LanguageFactory
) : LanguageDispatcher {
    private val languageApiSet: Map<SupportLanguages, LanguageRouterKtorfitApi> =
        SupportLanguages.entries.associateWith { language ->
            languageFactory.getLanguageApi(language)
        }

    override suspend fun <R> dispatch(language: SupportLanguages, on: suspend LanguageRouterKtorfitApi.() -> R): R {
        val api = languageApiSet[language]

        if (api == null) {
            throw RuntimeException("语言存在于 SupportLanguages 但服务未被发现")
        } else {
            return api.on()
        }
    }
}
