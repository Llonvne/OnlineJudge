package cn.llonvne.gojudge.exposed

import io.ktor.client.*
import kotlin.jvm.JvmInline

@JvmInline
value class RuntimeService(internal val httpClient: HttpClient)