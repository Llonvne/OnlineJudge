package cn.llonvne.site.team

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.kvision.service.*
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp.CreateGroupOk
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel
import io.kvision.core.Container
import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p
import io.kvision.routing.Routing
import kotlinx.serialization.Serializable

@Serializable
private data class CreateTeam(
    val teamName: String, val shortName: String, val visibilityStr: String, val typeStr: String, val description: String
)

fun teamCreate(root: Container, routing: Routing) {
    root.div {

        div(className = "row") {

            alert(AlertType.Info) {
                h1 {
                    +"创建队伍"
                }

                p {
                    +"创建小组/组织，来团结合作解决问题"
                }
            }

            div(className = "col-6") {

                alert(AlertType.Light) {
                    val form = formPanel<CreateTeam> {
                        add(CreateTeam::teamName, Text {
                            label = "你的组织的名字"
                        }, required = true)

                        observableOf("") {
                            add(
                                CreateTeam::shortName, sync(Text()) {
                                    label =
                                        "短名称,仅支持数字英文，最长不超过20个字符(你可以通过 /t/${it ?: "<短名称>"} 访问您的组织)"
                                    onChangeLaunch {
                                        setObv(this.value ?: "<短名称>")
                                    }
                                }, required = true
                            )
                        }

                        add(
                            CreateTeam::visibilityStr, TomSelect(
                                options = GroupVisibility.options, label = "小组可见性"
                            ), required = true
                        )

                        add(
                            CreateTeam::typeStr, TomSelect(
                                options = GroupType.options, label = "小组类型（对于个人用户，请选择经典小组）"
                            ), required = true
                        )

                        add(CreateTeam::description, Text {
                            label = "描述"
                            placeholder = "用一句话简单描述一下吧"
                        }, required = true)


                        button("提交") {
                            onClickLaunch {
                                form.clearValidation()
                                if (!form.validate()) {
                                    return@onClickLaunch
                                }
                                val createTeam = form.getData()
                                val resp = TeamModel.create(
                                    IGroupService.CreateGroupReq(
                                        groupName = createTeam.teamName,
                                        groupShortName = createTeam.shortName,
                                        teamVisibility = GroupVisibility.entries[createTeam.visibilityStr.toInt()],
                                        groupType = GroupType.entries[createTeam.typeStr.toInt()],
                                        description = createTeam.description
                                    )
                                )
                                when (resp) {
                                    is ClientError -> Messager.toastInfo(resp.message)
                                    is CreateGroupOk -> Messager.toastInfo("成功创建小组,小组ID为${resp.group.groupId}")
                                    PermissionDenied -> Messager.toastInfo("你没有创建改小组的权限，或者你还未登入")
                                    is InternalError -> Messager.toastInfo(resp.reason)
                                    is GroupShortNameUnavailable -> Messager.toastInfo("小组短名称${resp.shortName}已被占用")
                                }
                            }
                        }
                    }
                }
            }

            div(className = "col-6") {

            }
        }


    }
}