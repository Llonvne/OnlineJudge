package cn.llonvne.site

import cn.llonvne.model.AuthenticationModel
import io.kvision.Application
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.panel.root
import io.kvision.state.bind

fun Container.mine() {
    div().bind(AuthenticationModel.userToken) { token ->
        if (token == null) {
            h1 {
                +"您还未登入"
            }
        } else {
            +token.username
        }
    }
}