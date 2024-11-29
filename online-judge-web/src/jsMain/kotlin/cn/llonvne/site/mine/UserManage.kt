package cn.llonvne.site.mine

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badges
import cn.llonvne.compoent.defineColumn
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.ModifyUserForm
import cn.llonvne.entity.role.Backend
import cn.llonvne.entity.role.Banned
import cn.llonvne.entity.role.check
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.IMineService
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.MineModel
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.form.check.CheckBox
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.modal.Alert
import io.kvision.modal.Confirm
import io.kvision.modal.Dialog
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import kotlinx.coroutines.launch

data class UserManage(
    override val name: String = "用户管理",
) : AdminMineChoice {
    override fun show(root: Container) {
        root.div {
            alert(AlertType.Light) {
                h4 {
                    +"用户管理"
                }

                observableOf<IMineService.UsersResp>(null) {
                    setUpdater {
                        MineModel.users()
                    }

                    syncNotNull(div { }) { resp ->

                        when (resp) {
                            is IMineService.UsersResp.UsersRespImpl -> onSuccess(this, resp)
                        }
                    }
                }
            }
        }
    }

    private fun onSuccess(
        div: Div,
        resp: IMineService.UsersResp.UsersRespImpl,
    ) {
        div.alert(AlertType.Light) {
            tabulator(
                resp.users,
                options =
                    TabulatorOptions(
                        layout = Layout.FITDATASTRETCH,
                        columns =
                            listOf(
                                defineColumn("用户") {
                                    Span {
                                        +it.username
                                    }
                                },
                                defineColumn("创建时间") {
                                    Span {
                                        +it.createAt.ll()
                                    }
                                },
                                defineColumn("状态") {
                                    Span {
                                        if (it.userRole.roles.check(Banned.BannedImpl)) {
                                            +"被封禁"
                                        } else {
                                            +"正常"
                                        }
                                    }
                                },
                                defineColumn("是否为超级管理") {
                                    Span {
                                        if (it.userRole.roles.check(Backend.BackendImpl)) {
                                            +"是"
                                        } else {
                                            +"否"
                                        }
                                    }
                                },
                                defineColumn("操作") { user ->
                                    Span {
                                        badges {
                                            add(color = BadgeColor.Red) {
                                                +"删除"
                                                onClick {
                                                    Confirm.show(
                                                        "确认删除用户",
                                                        "你确定要删除用户 ${user.username}",
                                                        animation = false,
                                                        align = Align.LEFT,
                                                        yesTitle = "确认",
                                                        noTitle = "取消",
                                                        cancelVisible = false,
                                                        noCallback = {
                                                            Alert.show("删除用户通知", "你取消了删除用户的操作")
                                                        },
                                                    ) {
                                                        AppScope.launch {
                                                            if (MineModel.deleteUser(user.userId)) {
                                                                Alert.show("删除用户通知", "删除用户成功")
                                                            } else {
                                                                Alert.show("删除用户通知", "删除用户失败")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            add {
                                                +"修改"
                                                onClickLaunch {
                                                    val dialog =
                                                        Dialog<ModifyUserForm> {
                                                            val form =
                                                                formPanel<ModifyUserForm> {
                                                                    add(
                                                                        ModifyUserForm::userId,
                                                                        Text {
                                                                            label = "用户ID"
                                                                            value = user.userId.toString()
                                                                            disabled = true
                                                                        },
                                                                    )

                                                                    add(
                                                                        ModifyUserForm::username,
                                                                        Text {
                                                                            label = "用户名"
                                                                            value = user.username
                                                                        },
                                                                    )

                                                                    add(
                                                                        ModifyUserForm::isBanned,
                                                                        CheckBox(label = "是否封禁") {
                                                                            value = user.userRole.roles.check(Banned.BannedImpl)
                                                                        },
                                                                    )
                                                                }

                                                            button("确认") {
                                                                onClickLaunch {
                                                                    setResult(form.getData())
                                                                }
                                                            }
                                                            button("取消") {
                                                                onClickLaunch {
                                                                    setResult(null)
                                                                }
                                                            }
                                                        }
                                                    // TODO 重构！
                                                    if (MineModel.modifyUser(dialog.getResult())) {
                                                        Messager.toastInfo("设置成功")
                                                    } else {
                                                        Messager.toastInfo("设置失败")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            ),
                    ),
            )
        }
    }
}
