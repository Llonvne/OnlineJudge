package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.model.ProblemModel
import io.kvision.Application
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.launch

fun Container.detail(routing: Routing, id: Int) {

    val problem: ObservableValue<IProblemService.ProblemGetByIdResult?> = ObservableValue(null)

    AppScope.launch {
        problem.value = ProblemModel.getById(id)
    }

    div().bind(problem) {
        if (it == null) {
            h1 {
                +"Loading..."
            }

            navigateButton(routing, Frontend.Index)
        } else {
            when (it) {
                is IProblemService.ProblemGetByIdResult.Ok -> {
                    h1 {
                        +it.problem.problemName
                    }

                    p {
                        it.problem.problemDescription
                    }


                    navigateButton(routing, Frontend.Index)
                }

                IProblemService.ProblemGetByIdResult.ProblemNotFound -> {
                    h1 {
                        +"Problem NotFound"
                    }
                }
            }
        }
    }
}