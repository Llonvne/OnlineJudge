package cn.llonvne.compoent.layout

import io.kvision.core.*
import io.kvision.form.text.text
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.navbar.*
import io.kvision.theme.Theme
import io.kvision.theme.ThemeManager

fun Container.footer() {
    navbar(type = NavbarType.FIXEDBOTTOM) {
        nav {
            navLink("Llonvne/OnlineJudge","https://github.com/Llonvne/OnlineJudge")
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