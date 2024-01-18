package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Authentication
import cn.llonvne.constants.Frontend
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.types.ProblemStatus
import cn.llonvne.entity.types.ProblemStatus.*
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.ProblemModel
import io.kvision.Application
import io.kvision.core.*
import io.kvision.dropdown.dropDown
import io.kvision.form.check.checkBox
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.navbar.nav
import io.kvision.navbar.navForm
import io.kvision.navbar.navLink
import io.kvision.navbar.navbar
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.state.ObservableList
import io.kvision.state.ObservableListWrapper
import io.kvision.state.bind
import io.kvision.tabulator.*
import io.kvision.toolbar.buttonGroup
import io.kvision.toolbar.toolbar
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

internal fun Container.problems(routing: Routing) {
    val problems: ObservableList<ProblemListDto> = ObservableListWrapper()
    AppScope.launch {
        problems.addAll(
            ProblemModel.listProblem()
        )
    }
    toolbar(className = "p-1") {
        buttonGroup {
            button("创建您的题目").bind(AuthenticationModel.userToken) { token ->
                if (token == null) {
                    disabled = true
                }

                enableTooltip(
                    TooltipOptions(
                        title = "登入来创建属于你的题目",
                        rich = true,
                        delay = 0,
                        placement = Placement.BOTTOM,
                        triggers = listOf(Trigger.HOVER)
                    )
                )
            }
        }
    }

    div().bind(problems) {
        tabulator(
            it, options = TabulatorOptions(

                layout = Layout.FITCOLUMNS,
                columns = listOf(
                    ColumnDefinition("题目ID", "problem.problemId"),
                    ColumnDefinition("题目名称", "problem.problemName", cellClick = { _, cell ->
                        val id = cell.getData.invoke().asDynamic().problem.problemId as Int?
                        routing.navigate("/problems/${id}")
                    }),
                    ColumnDefinition("状态", "status", formatterComponentFunction = { _, _, e: ProblemListDto ->
                        span {
                            when (e.status) {
                                NotLogin -> {
                                    +"未登入"
                                }

                                NotBegin -> {
                                    +"未作答"
                                }

                                Accepted -> {
                                    +e.status.name
                                    color = Color.name(Col.GREEN)
                                }

                                WrongAnswer -> {
                                    +e.status.name
                                    color = Color.name(Col.RED)
                                }
                            }
                        }
                    }),
                )
            ), serializer = serializer()
        ) {
            height = 400.px
        }
    }
}