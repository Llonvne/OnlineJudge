package cn.llonvne

import cn.llonvne.constants.Frontend
import cn.llonvne.model.AuthenticationModel
import io.kvision.Application
import io.kvision.core.*
import io.kvision.dropdown.dropDown
import io.kvision.form.check.checkBox
import io.kvision.form.text.text
import io.kvision.html.div
import io.kvision.html.p
import io.kvision.navbar.*
import io.kvision.panel.root
import io.kvision.panel.stackPanel
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.theme.Theme
import io.kvision.theme.ThemeManager

fun Application.layout(routing: Routing, build: Container.() -> Unit) {
    root("kvapp") {
        navbar("OnlineJudge", type = NavbarType.STICKYTOP) {
            nav {
                navLink("主页", icon = "fas fa-file") {
                    onClick {
                        routing.navigate(Frontend.Index.uri)
                    }
                }
                navLink("题库", icon = "fas fa-file") {
                    onClick {
                        routing.navigate(Frontend.Problems.uri)
                    }
                }
                navLink("Edit", icon = "fas fa-bars")
                dropDown(
                    "Favourites",
                    listOf("HTML" to "#!/basic", "Forms" to "#!/forms"),
                    icon = "fas fa-star",
                    forNavbar = true
                )
            }
            navForm {
                text(label = "Search:")
                checkBox(label = "Search") {
                    inline = true
                }
            }

            div().bind(AuthenticationModel.userToken) { token ->
                if (token == null) {
                    nav(rightAlign = true) {
                        navLink("注册", icon = "fab fa-windows") {
                            onClick {
                                routing.navigate(Frontend.Register.uri)
                            }
                        }
                        navLink("登入", icon = "fab fa-windows") {
                            onClick {
                                routing.navigate(Frontend.Login.uri)
                            }
                        }
                    }
                } else {
                    nav(rightAlign = true) {
                        dropDown(
                            token.username,
                            listOf("HTML" to "#!/basic", "Forms" to "#!/forms"),
                            icon = "fas fa-star",
                            forNavbar = true
                        )
                        navLink("登出") {
                            onClick {
                                AuthenticationModel.logout()
                                this@nav.dispose()
                            }
                        }
                    }
                }
            }
        }
        div(className = "container") {
            build()
        }
        navbar(type = NavbarType.FIXEDBOTTOM) {
            nav {
                navText("Developed by Llonvne")
            }

            nav(rightAlign = true) {

                navLink("浅色") {
                    onClick {
                        ThemeManager.theme = Theme.LIGHT
                    }
                }
                navLink("深色") {
                    onClick {
                        ThemeManager.theme = Theme.DARK
                    }
                }

                navLink("自动") {
                    onClick {
                        ThemeManager.theme = Theme.AUTO
                    }
                }
            }
        }
    }
}