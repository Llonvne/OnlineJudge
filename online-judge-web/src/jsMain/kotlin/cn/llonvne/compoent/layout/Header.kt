package cn.llonvne.compoent.layout

import cn.llonvne.constants.Frontend
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.dropdown.dropDown
import io.kvision.html.div
import io.kvision.navbar.NavbarType
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.navbar.navbar
import io.kvision.routing.Routing
import io.kvision.state.bind

fun Container.header(routing: Routing) {
    navbar("OnlineJudge", type = NavbarType.STICKYTOP) {
        nav {
            navLink("主页", icon = "fas fa-house fa-solid") {
                onClick {
                    routing.navigate(Frontend.Index.uri)
                }
            }
            navLink("题库", icon = "fas fa-code fa-solid") {
                onClick {
                    routing.navigate(Frontend.Problems.uri)
                }
            }
            navLink("提交", icon = "fas fa-bars") {
                onClick {
                    routing.navigate(Frontend.Submission.uri)
                }
            }
            navLink("训练场", icon = "fas fa-play") {
                onClick {
                    routing.navigate(Frontend.PlayGround.uri)
                }
            }
            dropDown(
                "Favourites",
                listOf("HTML" to "#!/basic", "Forms" to "#!/forms"),
                icon = "fas fa-star",
                forNavbar = true
            )
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
                        listOf("我的主页" to "#/me", "Forms" to "#!/forms"),
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
}