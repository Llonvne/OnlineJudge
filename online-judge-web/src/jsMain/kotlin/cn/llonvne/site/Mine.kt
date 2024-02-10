package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp.Login
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.launch

fun Container.mine() {
    div().bind(AuthenticationModel.userToken) { token ->
        if (token == null) {
            h1 {
                +"您还未登入"
            }
        } else {

            val info = ObservableValue<Login?>(null)
            AppScope.launch {
                info.value = AuthenticationModel.info()
            }
            div().bind(info) {
                if (it != null) {
                    +it.username
                }
            }

        }
    }
}