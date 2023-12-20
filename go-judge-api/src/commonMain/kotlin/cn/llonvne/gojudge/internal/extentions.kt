package cn.llonvne.gojudge.internal

import io.ktor.client.request.*
import io.ktor.http.*

fun HttpRequestBuilder.setContextTypeApplicationJson() {
    contentType(ContentType.parse("application/json"))
}