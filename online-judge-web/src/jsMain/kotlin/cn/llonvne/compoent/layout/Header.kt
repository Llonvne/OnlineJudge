package cn.llonvne.compoent.layout

import cn.llonvne.AppScope
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.constants.Frontend
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp.Login
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.site.loginPanel
import io.kvision.core.Container
import io.kvision.dropdown.dropDown
import io.kvision.html.div
import io.kvision.navbar.*
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.launch

private fun Navbar.showNavigator(routing: Routing) {
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
        dropDown(
            "比赛",
            listOf(
                "比赛信息" to "#/contest",
                "创建" to "#/contest/create"
            ),
            icon = "fas fa-star",
            forNavbar = true
        )
        navLink("训练场", icon = "fas fa-play") {
            onClick {
                routing.navigate(Frontend.PlayGround.uri)
            }
        }
        dropDown(
            "队伍",
            listOf(
                "队伍" to "#/team",
                "创建" to "#/team/create",
            ),
            icon = "fas fa-star",
            forNavbar = true
        )
    }
}

fun Container.header(routing: Routing) {
    div { }.bind(AuthenticationModel.userToken) { token ->
        navbar("OnlineJudge", type = NavbarType.STICKYTOP) {
            showNavigator(routing)

            if (token == null) {
                nav(rightAlign = true) {
                    navLink("注册", icon = "fab fa-windows") {
                        onClick {
                            routing.navigate(Frontend.Register.uri)
                        }
                    }
                    navLink("登入", icon = "fab fa-windows") {
                        onClick {
                            loginPanel()
                        }
                    }
                }
            } else {

                observableOf<Login>(null) {
                    setUpdater {
                        AuthenticationModel.info()
                    }

                    sync(nav(rightAlign = true) { }) { info ->
                        dropDown(
                            info?.username.toString(),
                            listOf(
                                "我的主页" to "#/me",
                                "Forms" to "#!/forms",
                            ),
                            icon = "fas fa-star",
                            forNavbar = true
                        )
                        navLink("登出") {
                            onClick {
                                Messager.toastInfo(AuthenticationModel.logout().toString())
                            }
                        }
                    }
                }
            }
        }
    }
}