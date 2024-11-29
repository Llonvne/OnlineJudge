package cn.llonvne.gojudge.internal

import io.ktor.client.*
import kotlin.jvm.JvmInline

@JvmInline
value class GoJudgeClient(
    internal val httpClient: HttpClient,
)
