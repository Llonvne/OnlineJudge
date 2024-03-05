package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.ObservableDsl
import cn.llonvne.kvision.service.IContestService.LoadContestResp.LoadOk
import cn.llonvne.site.contest.Display
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.html.button
import io.kvision.html.div

interface ProblemChooser {
    fun show(container: Container, observableDsl: ObservableDsl<Display>)

    companion object {
        fun from(loadOk: LoadOk): ProblemChooser = BaseProblemChooser(loadOk)
    }
}

private class BaseProblemChooser(private val loadOk: LoadOk) : ProblemChooser {
    override fun show(container: Container, observableDsl: ObservableDsl<Display>) {
        container.div {
            alert(AlertType.Light) {
                loadOk.contest.context.problems.forEachIndexed { index, problem ->
                    button('A'.plus(index).toString()) {
                        onClickLaunch {
                            observableDsl.setObv(Display.ProblemIndex(problem.problemId))
                        }
                    }
                }
            }
        }
    }
}