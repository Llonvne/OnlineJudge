package cn.llonvne.gojudge.exposed

import io.ktor.client.request.*
import io.ktor.http.*

fun HttpRequestBuilder.setContextTypeApplicationJson() {
    contentType(ContentType.parse("application/json"))
}