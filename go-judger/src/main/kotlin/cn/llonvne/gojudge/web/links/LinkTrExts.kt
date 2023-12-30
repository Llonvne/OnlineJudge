package cn.llonvne.gojudge.web.links

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.A

internal fun LinkTreeConfigurer.linkIn(name: String, decr: String, url: String, render: A.() -> Unit = {}) = link(
    LinkEntity(name, decr, url, render)
)

context(LinkTreeConfigurer)
internal fun Route.get(
    path: String,
    decr: String,
    render: A.() -> Unit = {},
    body: PipelineInterceptor<Unit, ApplicationCall>
) {
    linkIn(path, decr, this.toString() + path, render)
    get(path, body)
}

context(LinkTreeConfigurer)
internal fun Route.post(
    path: String,
    decr: String,
    render: A.() -> Unit = {},
    body: PipelineInterceptor<Unit, ApplicationCall>
) {
    linkIn(path, decr, this.toString() + path, render)
    post(path, body)
}