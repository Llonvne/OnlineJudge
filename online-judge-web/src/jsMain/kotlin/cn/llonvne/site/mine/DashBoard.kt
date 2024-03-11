package cn.llonvne.site.mine

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.kvision.service.IMineService
import cn.llonvne.kvision.service.IMineService.DashboardResp.DashboardRespImpl
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.MineModel
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.h4

data class DashBoard(override val name: String = "仪表盘") : AdminMineChoice {


    override fun show(root: Container) {

        observableOf<IMineService.DashboardResp>(null) {
            setUpdater { MineModel.dashboard() }

            syncNotNull(root.div { }) { resp ->

                when (resp) {
                    is DashboardRespImpl -> onSuccess(resp)
                    PermissionDenied -> Messager.toastInfo("你未登入，或者不具有后台权限")
                }
            }
        }
    }

    private fun Div.onSuccess(resp: DashboardRespImpl) {
        alert(AlertType.Light) {

            h4 {
                +"统计数据"
            }

            div(className = "row") {
                div(className = "col") {
                    alert(AlertType.Info) {
                        h4 {
                            +"用户总数: ${resp.statistics.totalUserCount}"
                        }
                    }
                }
                div(className = "col") {
                    alert(AlertType.Success) {
                        h4 {
                            +"今日总提交: ${resp.statistics.totalSubmissionToday}"
                        }
                    }
                }
                div(className = "col") {
                    alert(AlertType.Secondary) {
                        h4 {
                            +"近两周比赛: ${resp.statistics.totalContestLastTwoWeek}"
                        }
                    }
                }
            }
        }

        alert(AlertType.Light) {

            h4 {
                +"后端系统"
            }

            div(className = "row") {
                div(className = "col") {
                    alert(AlertType.Success) {
                        +"名称: ${resp.backendInfo.name}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Info) {
                        +"地址: ${resp.backendInfo.host}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Secondary) {
                        +"端口: ${resp.backendInfo.port}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Danger) {
                        +"CPU 使用率: ${resp.backendInfo.cpuUsage}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Warning) {
                        +"CPU 核心数: ${resp.backendInfo.cpuCoresCount}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Primary) {
                        +"使用中的内存: ${resp.backendInfo.usedMemory / 1048576} MB"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Dark) {
                        +"总内存: ${resp.backendInfo.totalMemory / 1048576} MB"
                    }
                }
            }
        }

        alert(AlertType.Light) {

            h4 {
                +"评测机"
            }

            div(className = "row") {
                div(className = "col") {
                    alert(AlertType.Success) {
                        +"名称: ${resp.judgeServerInfo.name}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Info) {
                        +"地址: ${resp.judgeServerInfo.host}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Secondary) {
                        +"端口: ${resp.judgeServerInfo.port}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Danger) {
                        +"CPU 使用率: ${resp.judgeServerInfo.cpuUsage}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Warning) {
                        +"CPU 核心数: ${resp.judgeServerInfo.cpuCoresCount}"
                    }
                }
                div(className = "col") {
                    alert(AlertType.Primary) {
                        +"使用中的内存: ${resp.judgeServerInfo.memoryUsage / 1048576} MB"
                    }
                }
            }
        }
    }
}