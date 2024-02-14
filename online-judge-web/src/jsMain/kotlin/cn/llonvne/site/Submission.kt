package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.observableListOf
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.*
import io.kvision.routing.Routing
import io.kvision.state.ObservableListWrapper
import io.kvision.state.bind
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer


fun Container.submission(routing: Routing) {

    observableListOf {
        setUpdater {
            SubmissionModel.list()
        }

        div(className = "p-1") { }.bind(this) {
            tabulator<SubmissionListDto>(
                it,
                options = TabulatorOptions(
                    layout = Layout.FITDATASTRETCH,
                    columns = listOf(
                        ColumnDefinition("提交ID", formatterComponentFunction = { _, _, e: SubmissionListDto ->
                            span {
                                +(e.submissionId.toString())
                            }
                        }),
                        ColumnDefinition("题目名字", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.problemName

                                onClick {
                                    routing.navigate("/problems/${e.problemId}")
                                }
                            }
                        }),
                        ColumnDefinition("状态", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.status.name
                            }
                        }),
                        ColumnDefinition("语言", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.language.toString()
                            }
                        }),
                        ColumnDefinition("运行时间", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.runTime
                            }
                        }),
                        ColumnDefinition("运行内存", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.runMemory
                            }
                        }),
                        ColumnDefinition("代码长度", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.codeLength.toString()
                            }
                        }),
                        ColumnDefinition("作者", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.user.username
                            }
                        }),
                        ColumnDefinition("提交时间", formatterComponentFunction = { _, _, e ->
                            div {
                                +"${e.submitTime.year}年${e.submitTime.monthNumber}月${e.submitTime.dayOfMonth}日${e.submitTime.hour}时${e.submitTime.minute}分${e.submitTime.second}秒"
                            }
                        }),
                        ColumnDefinition("", formatterComponentFunction = { _, _, e ->
                            link("详情", className = "p-1") {
                                onClick {
                                    routing.navigate("/code/${e.submissionId}")
                                }
                            }
                        })
                    ),
                ),
                serializer = serializer()
            ) {
                height = 400.px
            }
        }
    }

}