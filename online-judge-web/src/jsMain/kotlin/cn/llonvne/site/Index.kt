package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.message.Messager
import io.kvision.core.Container
import io.kvision.form.select.tomSelect
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.routing.Routing

fun Container.index(routing: Routing) {
    alert(AlertType.Light) {
        h1 {
            +"Online Judge"
        }
    }

    div(className = "row") {
        div(className = "col") {
            alert(AlertType.Secondary) {
                h4 {
                    +"作为个人用户"
                }

                p {
                    +"点击左上角注册账号，立刻开始使用"
                }

                h6 {
                    +"功能介绍"
                }

                ul {
                    li {
                        +"题库:查找有兴趣的题目"
                    }
                    li {
                        +"提交:查看你的所有题目的提交"
                    }
                    li {
                        +"训练场:在线测试您的代码"
                    }
                }
            }

            alert(type = AlertType.Warning) {
                h4 {
                    +"查看我们的免费课程"
                }
            }
        }
        div(className = "col") {
            alert(AlertType.Info) {
                h4 {
                    +"作为团队/学校用户"
                }

                h6 {
                    +"输入您的通行凭证(xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"
                }

                val text = text(InputType.TEXT)

                div {
                    button("转到", style = ButtonStyle.OUTLINESECONDARY) {
                        onClick {
                            if (text.value?.length == 36) {
                                routing.navigate("/team/${text.value}")
                            } else {
                                Messager.toastInfo("不是合法的凭证")
                            }
                        }
                    }
                }

                br { }

                h6 {
                    +"查找您的组织"
                }

                label {
                    +"请注意如果团队/学校选择了不公开其组织名称，那么您只能通过通行凭证加入"
                }

                tomSelect {
                }

                h6 {
                    +"新建团队/学校"
                }

                label {
                    +"建立您自己的团队/学校"
                }

                br { }

                button("新建", style = ButtonStyle.OUTLINESECONDARY) {
                    onClick {
                        routing.navigate("/team/create")
                    }
                }
            }
        }
    }
}
