package cn.llonvne.site.contest

import cn.llonvne.compoent.*
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.contest.HashId
import cn.llonvne.entity.contest.IntId
import cn.llonvne.kvision.service.ContestNotFound
import cn.llonvne.kvision.service.ContestOwnerNotFound
import cn.llonvne.kvision.service.IContestService
import cn.llonvne.kvision.service.IContestService.LoadContestResp.LoadOk
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.model.ContestModel
import cn.llonvne.site.contest.Display.*
import cn.llonvne.site.contest.detail.ContestDetailHeader
import cn.llonvne.site.contest.detail.ContestProblemDisplay
import cn.llonvne.site.contest.detail.ContestStatusResolver
import cn.llonvne.site.contest.detail.ProblemChooser
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.html.button
import io.kvision.html.div
import cn.llonvne.site.contest.Display as ContestDisplay

sealed interface ContestDetail {
    fun show(root: Container)

    companion object {
        private data object ContestNotFound : ContestDetail {
            override fun show(root: Container) {
                root.notFound(
                    object : NotFoundAble {
                        override val header: String
                            get() = "比赛未找到"
                        override val notice: String
                            get() = "请确认比赛ID正确，如果确认比赛ID正确，请联系我们 ^_^"
                        override val errorCode: String = "ContestNotFound"
                    },
                )
            }
        }

        fun from(id: String): ContestDetail {
            val intId = id.toIntOrNull()

            val contestId: ContestId =
                if (intId != null) {
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

sealed interface Display {
    data class ProblemIndex(
        val id: Int,
    ) : ContestDisplay

    data object Status : ContestDisplay

    data object None : ContestDisplay
}

private class BaseContestDetail(
    private val contestId: ContestId,
) : ContestDetail {
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
                        ContestNotFound ->
                            notFound(
                                "比赛未找到",
                                "请检查比赛ID是否正确，如果确认比赛ID正确，请联系我们.",
                                "ContestNotFound-$contestId",
                            )

                        is LoadOk -> {
                            onOk(this, resp)
                        }

                        PermissionDenied -> notFound("你还为登入", "请登入后查看该页面", "NotLogin")
                        ContestOwnerNotFound ->
                            notFound(
                                "所有者账号处于异常状态，该比赛已经不可查看",
                                "如有问题请联系管理",
                                "OwnerIdNotFound",
                            )
                    }
                }
            }
        }
    }

    fun onOk(
        container: Container,
        loadOk: LoadOk,
    ) {
        val contestDetailHeader = ContestDetailHeader.form(loadOk = loadOk)

        val problemChooser = ProblemChooser.from(loadOk)

        val statusResolver = ContestStatusResolver(loadOk.contest.startAt, loadOk.contest.endAt)

        val problemDisplay = ContestProblemDisplay.from(contestId, statusResolver)

        container.div {
            contestDetailHeader.show(div { })

            observableOf<ContestDisplay>(None) {
                setUpdater {
                    ProblemIndex(
                        loadOk.contest.context.problems
                            .first()
                            .problemId,
                    )
                }
                sync(div { }) { index ->
                    div(className = "row") {
                        div(className = "col-3") {
                            problemChooser.show(div { }, this@observableOf)

                            alert(AlertType.Light) {
                                button("状态") {
                                    onClickLaunch {
                                        setObv(Status)
                                    }
                                }
                            }
                        }
                        div(className = "col") {
                            if (index != null) {
                                when (index) {
                                    None -> loading()
                                    is ProblemIndex -> problemDisplay.show(div { }, index)
                                    Status -> TODO()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
