package cn.llonvne.gojudge.web.links

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

/**
 * @property name 链接的名称
 * @property decr 链接的描述
 * @property uri 链接的地址
 * @property render 自定义渲染函数，可选
 */
internal data class LinkEntity(val name: String, val decr: String, val uri: String, val render: A.() -> Unit = {})

/**
 * 链接树描述器
 */
internal interface LinkTreeConfigurer {
    /**
     * 添加一个链接目标
     */
    fun link(linkEntity: LinkEntity)

    /**
     * 获取所有链接
     */
    fun getLink(): List<LinkEntity>

    /**
     * 调整样式
     */
    fun name(): H1.() -> Unit = {}

    /**
     * 调整样式
     */
    fun decr(): H4.() -> Unit = {}

    /**
     * 调整样式
     */
    fun link(): A.() -> Unit = {}
}

context(LinkTreeAware)
internal fun Route.linkTr(
    name: String = "Links",
    decr: String = "LinkTree",
    configurer: LinkTreeConfigurer = LinkTreeConfigurerImpl(),
    build: LinkTreeConfigurer.() -> Unit,
) {
    linkTr(linkTreeUri, name, decr, configurer, build)
}

/**
 * @param url 链接树的地址
 * @param decr 链接树的描述
 * @param build 构建链接树的函数
 * @param configurer 允许用户自定义存储类型，默认使用 list
 *
 * 在 [url] 建立一颗链接树，以[build]描述
 */
internal fun Route.linkTr(
    url: String,
    name: String = "Links",
    decr: String = "LinkTree",
    configurer: LinkTreeConfigurer = LinkTreeConfigurerImpl(),
    build: LinkTreeConfigurer.() -> Unit,
) {
    configurer.build()

    get(url) {
        call.respondHtml {
            body {
                h1 {
                    +name

                    configurer.name().invoke(this)
                }

                h4 {
                    +decr

                    configurer.decr().invoke(this)
                }

                configurer.getLink().forEach { link ->
                    div {
                        a {
                            href = link.uri
                            +link.name

                            configurer.link().invoke(this)
                            link.render.invoke(this)
                        }
                    }
                }
            }
        }
    }
}

private class LinkTreeConfigurerImpl : LinkTreeConfigurer {

    private val links = mutableListOf<LinkEntity>()

    override fun link(linkEntity: LinkEntity) {
        links.add(linkEntity)
    }

    override fun getLink(): List<LinkEntity> = links
}