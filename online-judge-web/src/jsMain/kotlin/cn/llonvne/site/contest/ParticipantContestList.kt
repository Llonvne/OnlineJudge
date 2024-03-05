package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.defineColumn
import cn.llonvne.compoent.notFound
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.ISubmissionService.GetParticipantContestResp.GetParticipantContestOk
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.RoutingModule
import cn.llonvne.model.SubmissionModel
import cn.llonvne.site.contest.detail.ContestStatusResolver
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.html.Span
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.html.p
import io.kvision.state.bind
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator

fun participantContestList(root: Container) {
    root.div().bind(AuthenticationModel.userToken) {
        if (it == null) {
            alert(AlertType.Info) {
                h4 {
                    +"你还未登入，无法获取已经参加的比赛"
                }
            }
        } else {
            alert(AlertType.Info) {
                h4 {
                    +"您参加的比赛"
                }
                p {
                    +"你必须在比赛中作出一次有效提交才能计入参加比赛"
                }

                observableOf<ISubmissionService.GetParticipantContestResp>(null) {
                    setUpdater { SubmissionModel.getParticipantContest() }

                    sync(div { }) { resp ->
                        when (resp) {
                            is GetParticipantContestOk -> onOk(div { }, resp)
                            PermissionDenied -> notFound("你还为登入", "登入后才可以查看参加的比赛哦", "NotLogin")
                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

private fun onOk(root: Container, resp: GetParticipantContestOk) {


    root.tabulator(
        resp.contests, options = TabulatorOptions(
            layout = Layout.FITCOLUMNS,
            columns = listOf(
                defineColumn("比赛名") {
                    Span {
                        +it.title

                        val id = it.contestId

                        onClickLaunch {
                            RoutingModule.routing.navigate("/contest/$id")
                        }
                    }
                },
                defineColumn("状态") {
                    val contestStatusResolver = ContestStatusResolver(it.startAt, it.endAt)
                    Span {
                        +contestStatusResolver.status().name
                    }
                },
                defineColumn("AC 数量") {
                    Span {

                    }
                },
                defineColumn("题目总数") {
                    Span {
                        +it.context.problems.size.toString()
                    }
                }
            )
        )
    )
}