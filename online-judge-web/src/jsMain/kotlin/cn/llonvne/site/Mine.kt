package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.loading
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.kvision.service.IAuthenticationService.MineResp
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.html.p

fun Container.mine() {
    observableOf<MineResp>(null) {
        setUpdater {
            AuthenticationModel.mine()
        }

        sync {
            if (it == null) {
                loading()
            } else {
                Mine.from(it).load(div { })
            }
        }
    }
}

private interface Mine {

    fun load(root: Container)

    companion object {

        private val notLogin = object : Mine {
            override fun load(root: Container) {
                root.alert(AlertType.Danger) {
                    h4 {
                        +"您还未登入"
                    }

                    p {
                        +"请先登入账户后刷新页面"
                    }
                }
            }
        }

        fun from(mineResp: MineResp): Mine {
            return when (mineResp) {
                is MineResp.AdministratorMineResp -> AdministratorMine()
                is MineResp.NormalUserMineResp -> NormalUserMine(mineResp)
                PermissionDenied -> notLogin
            }
        }
    }
}

private class AdministratorMine : Mine {
    override fun load(root: Container) {
        root.div {
            alert(AlertType.Light) {
                h4 {
                    +"Load as Admin"
                }
            }
        }
    }
}

private class NormalUserMine(private val mineResp: MineResp.NormalUserMineResp) : Mine {
    override fun load(root: Container) {
        root.div {
            alert(AlertType.Light) {
                h4 {
                    +"Load as Normal User"
                }
            }
        }
    }
}

