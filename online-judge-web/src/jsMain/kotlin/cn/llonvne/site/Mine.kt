package cn.llonvne.site

import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp.Login
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container

fun Container.mine() {
    observableOf<Login?>(null) {
        setUpdater {
            AuthenticationModel.info()
        }

        sync {
            if (it == null) {
                +"你还未登入"
            } else {
                +it.username
            }
        }
    }
}