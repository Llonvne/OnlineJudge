package cn.llonvne.site.mine

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.loading
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.kvision.service.IAuthenticationService.MineResp
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.toolbar.buttonGroup

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
                is MineResp.Administrator -> AdministratorMine()
                is MineResp.NormalUser -> NormalUserMine(mineResp)
                PermissionDenied -> notLogin
            }
        }
    }
}


class AdministratorMine : Mine {
    override fun load(root: Container) {
        root.div {
            observableOf<AdminMineChoice>(null) {
                setUpdater { AdminMineChoice.defaultChoice() }
                alert(AlertType.Light) {
                    h1 {
                        +"Online Judge 管理面板"
                    }
                    buttonGroup {
                        AdminMineChoice.getAdminMineChoices().forEach { choice ->
                            button(choice.name) {
                                onClick {
                                    setObv(choice)
                                }
                            }
                        }
                    }
                }

                syncNotNull(div { }) { choice ->
                    choice.show(this)
                }
            }

        }
    }
}

private class NormalUserMine(private val mineResp: MineResp.NormalUser) : Mine {
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

