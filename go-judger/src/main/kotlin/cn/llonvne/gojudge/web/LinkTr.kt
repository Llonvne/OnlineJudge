package cn.llonvne.gojudge.web

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
data class Link(val name: String, val decr: String, val uri: String, val render: A.() -> Unit = {})

/**
 * 链接树描述器
 */
interface LinkTreeConfigurer {
    /**
     * 添加一个链接目标
     */
    fun link(link: Link)
}

/**
 * @param url 链接树的地址
 * @param decr 链接树的描述
 * @param build 构建链接树的函数
 *
 * 在 [url] 建立一颗链接树，以[build]描述
 */
fun Route.linkTr(url: String, name: String = "Links", decr: String = "LinkTree", build: LinkTreeConfigurer.() -> Unit) {

    val configurer = LinkTreeConfigurerImpl()
    configurer.build()

    get(url) {
        call.respondHtml {
            body {
                h1 {
                    +name
                }

                h4 {
                    +decr
                }

                configurer.getLink().forEach { link ->
                    a {
                        href = link.uri
                        +link.name

                        link.render.invoke(this)
                    }
                }
            }
        }
    }
}

private class LinkTreeConfigurerImpl() : LinkTreeConfigurer {

    private val links = mutableListOf<Link>()

    override fun link(link: Link) {
        links.add(link)
    }

    fun getLink(): List<Link> = links
}