package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.defineColumn
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestContext
import cn.llonvne.kvision.service.*
import cn.llonvne.kvision.service.IContestService.AddProblemResp.AddOkResp
import cn.llonvne.kvision.service.ISubmissionService.ProblemNotFound
import cn.llonvne.kvision.service.AddProblemPermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.ContestModel
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.html.*
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.js.Date

@Serializable
data class CreateContestForm(
    val title: String,
    val description: String = "",
    @Contextual val startAt: Date,
    @Contextual val endAt: Date,
    val contestScoreTypeStr: String,
    val rankTypeStr: String
)

@Serializable
private data class AddContestProblemForm(
    val problemId: String
)

fun Container.createContest() {
    alert(AlertType.Success) {
        h1 {
            +"创建你自己的比赛"
        }

        p {
            +"在限定的时间内与别人一决高下吧"
        }
    }

    observableOf(listOf<ContestContext.ContestProblem>()) {
        div(className = "row") {
            div(className = "col") {
                alert(AlertType.Light) {
                    val form = formPanel<CreateContestForm> {
                        add(CreateContestForm::title, Text(label = "比赛标题"), required = true)
                        add(CreateContestForm::description, RichText(label = "描述"), required = true)
                        add(CreateContestForm::startAt, DateTime(label = "开始时间"), validator = { dateTime ->
                            val utc = dateTime.value?.getTime()
                            if (utc == null) {
                                false
                            } else {
                                utc > Date.now()
                            }
                        }, validatorMessage = { dateTime ->
                            "比赛开始时间必须晚于现在"
                        })
                        add(CreateContestForm::endAt, DateTime(label = "结束时间"), validator = { dateTime ->
                            val utc = dateTime.value?.getTime()
                            if (utc == null) {
                                false
                            } else {
                                utc > Date.now() && utc > form.getData().startAt.getTime()
                            }
                        }, validatorMessage = { dateTime -> "比赛开始时间必须晚于现在且晚于开始时间" })
                        add(
                            CreateContestForm::contestScoreTypeStr,
                            TomSelect(label = "记分方式", options = Contest.ContestScoreType.entries.map {
                                it.name to it.name
                            })
                        )
                        add(
                            CreateContestForm::rankTypeStr,
                            TomSelect(label = "排行显示", options = Contest.ContestRankType.entries.map {
                                it.name to it.name
                            })
                        )

                        button("提交", style = ButtonStyle.OUTLINESECONDARY) {
                            onClickLaunch {
                                if (!form.validate()) {
                                    return@onClickLaunch
                                }
                                val data = form.getData()
                                val problems = getState()
                                if (problems.isNullOrEmpty()) {
                                    return@onClickLaunch Messager.toastInfo("必须要有一道题目")
                                }

                                when (val resp = ContestModel.create(data, problems)) {
                                    is IContestService.CreateContestResp.CreateOk -> {
                                        Messager.toastInfo("创建比赛成功")
                                        RoutingModule.routing.navigate("/contest/${resp.contest.contestId}")
                                    }

                                    is InternalError -> Messager.toastInfo(resp.reason)
                                    PermissionDenied -> Messager.toastInfo("请先登入后在创建题目")
                                    ProblemIdInvalid -> Messager.toastInfo("输入题目的ID无效")
                                }
                            }
                        }

                        getChildren().forEach {
                            it.addCssClass("small")
                        }
                    }
                }
            }

            div(className = "col") {
                alert(AlertType.Light) {
                    h4 {
                        +"选择题目"
                    }
                    val addProblemForm = formPanel<AddContestProblemForm> {
                        add(AddContestProblemForm::problemId, Text(label = "题目ID"), required = true)
                    }


                    setUpdater { listOf() }
                    sync(div { }) { problems ->
                        if (problems == null) {
                            +"<题目集为空>"
                        }

                        tabulator(
                            problems, options = TabulatorOptions(
                                columns = listOf(
                                    defineColumn("题目ID") {
                                        Span {
                                            +it.problemId.toString()
                                        }
                                    },
                                    defineColumn("题目名字") {
                                        Span {
                                            +it.alias
                                        }
                                    },
                                    defineColumn("题目权重") {
                                        Span {
                                            +it.weight.toString()
                                        }
                                    }
                                ), layout = Layout.FITCOLUMNS
                            )
                        )
                    }

                    button("添加", style = ButtonStyle.OUTLINESECONDARY) {
                        onClickLaunch {
                            when (val resp = ContestModel.addProblem(addProblemForm.getData().problemId)) {
                                is AddOkResp -> {
                                    if (getState()?.none { it.problemId == resp.problemId } != false) {
                                        setObv(
                                            listOf(
                                                ContestContext.ContestProblem(
                                                    problemId = resp.problemId,
                                                    weight = 1,
                                                    alias = resp.problemName
                                                )
                                            ) + (getState() ?: listOf())
                                        )
                                    } else {
                                        Messager.toastInfo("请勿添加重复的题目哦")
                                    }
                                }

                                AddProblemPermissionDenied -> Messager.toastInfo("该题目设置了权限，您无法添加该题目到您的题集中")
                                is InternalError -> Messager.toastInfo(resp.reason)
                                PermissionDenied -> Messager.toastInfo("你需要先登入才能创建比赛")
                                ProblemIdInvalid -> Messager.toastInfo("您输入了无效的题目ID")
                                ProblemNotFound -> Messager.toastInfo("你输入的题目ID未被找到")
                            }
                        }
                    }
                }
            }
        }
    }
}