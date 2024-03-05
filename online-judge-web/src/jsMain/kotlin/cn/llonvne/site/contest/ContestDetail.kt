package cn.llonvne.site.contest

import cn.llonvne.compoent.*
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.compoent.submission.SubmitProblemResolver
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.Contest.ContestStatus.*
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.contest.HashId
import cn.llonvne.entity.contest.IntId
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.ContestNotFound
import cn.llonvne.kvision.service.ContestOwnerNotFound
import cn.llonvne.kvision.service.IContestService
import cn.llonvne.kvision.service.IContestService.LoadContestResp.LoadOk
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.ll
import cn.llonvne.model.ContestModel
import cn.llonvne.site.problem.detail.CodeEditorShower
import cn.llonvne.site.problem.detail.CodeEditorShower.Companion.CodeEditorConfigurer
import cn.llonvne.site.problem.detail.detail
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.html.*
import kotlinx.datetime.Instant

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.js.Date


sealed interface ContestDetail {
    fun show(root: Container)

    companion object {

        private data object ContestNotFound : ContestDetail {
            override fun show(root: Container) {
                root.notFound(object : NotFoundAble {
                    override val header: String
                        get() = "比赛未找到"
                    override val notice: String
                        get() = "请确认比赛ID正确，如果确认比赛ID正确，请联系我们 ^_^"
                    override val errorCode: String = "ContestNotFound"
                })
            }
        }

        fun from(id: String): ContestDetail {
            val intId = id.toIntOrNull()

            val contestId: ContestId = if (intId != null) {
                IntId(intId)
            } else if (id.length == 36) {
                HashId(id)
            } else {
                return ContestNotFound
            }

            return BaseContestDetail(contestId)
        }
    }
}

private class BaseContestDetail(private val contestId: ContestId) : ContestDetail {

    override fun show(root: Container) {

        observableOf<IContestService.LoadContestResp>(null) {
            setUpdater {
                ContestModel.load(contestId)
            }

            sync(root.div { }) { resp ->
                if (resp == null) {
                    loading()
                } else {
                    when (resp) {
                        ContestNotFound -> notFound(
                            "比赛未找到",
                            "请检查比赛ID是否正确，如果确认比赛ID正确，请联系我们.",
                            "ContestNotFound-$contestId"
                        )

                        is LoadOk -> {
                            onOk(this, resp)
                        }

                        PermissionDenied -> notFound("你还为登入", "请登入后查看该页面", "NotLogin")
                        ContestOwnerNotFound -> notFound(
                            "所有者账号处于异常状态，该比赛已经不可查看",
                            "如有问题请联系管理",
                            "OwnerIdNotFound"
                        )
                    }
                }
            }
        }
    }

    fun onOk(container: Container, loadOk: LoadOk) {
        container.div {

            val instant = Instant.fromEpochMilliseconds(Date.now().toLong())
            val status: Contest.ContestStatus =
                if (instant < loadOk.contest.startAt.toInstant(TimeZone.currentSystemDefault())) {
                    NotBegin
                } else if (instant < loadOk.contest.endAt.toInstant(TimeZone.currentSystemDefault())) {
                    Running
                } else {
                    Ended
                }

            alert(
                when (status) {
                    NotBegin -> AlertType.Info
                    Running -> AlertType.Success
                    Ended -> AlertType.Danger
                }
            ) {
                h1 {
                    +loadOk.contest.title
                }

                h4(rich = true) {
                    +loadOk.contest.description
                }

                badges {
                    add {
                        +status.name
                    }
                    add {
                        +"所有者:${loadOk.ownerName}"
                    }
                    add {
                        +loadOk.contest.contestScoreType.name
                    }
                    add {
                        +loadOk.contest.rankType.chinese
                    }
                    add {
                        +"开始于${loadOk.contest.startAt.ll()}"
                    }
                    add {
                        +"结束于${loadOk.contest.endAt.ll()}"
                    }
                }
            }

            observableOf<IContestService.ContextSubmissionResp>(null) {
                setUpdater { ContestModel.contextSubmissions(contestId) }

                sync(div { }) { resp ->
                    if (resp != null) {
                        console.log(resp)
                    }
                }
            }

            observableOf(loadOk.contest.context.problems.first().problemId) {
                setUpdater { loadOk.contest.context.problems.first().problemId }
                sync(div { }) { index ->
                    div(className = "row") {
                        div(className = "col-3") {
                            alert(AlertType.Light) {
                                loadOk.contest.context.problems.forEachIndexed { index, problem ->
                                    button('A'.plus(index).toString()) {
                                        onClickLaunch {
                                            setObv(problem.problemId)
                                        }
                                    }
                                }
                            }
                        }
                        div(className = "col") {
                            if (index != null && index > 0) {
                                detail(div { }, index) {
                                    disableHistory = true
                                    submitProblemResolver = SubmitProblemResolver(contestId = contestId)
                                    codeEditorConfigurer = CodeEditorConfigurer {
                                        forceVisibility = SubmissionVisibilityType.Contest
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}