package cn.llonvne.compoent.observable

import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.model.ProblemModel
import io.kvision.state.ObservableState

fun observableProblemOf(id: Int, action: (problem: ObservableState<ProblemGetByIdResult?>) -> Unit) {
    observableOf<ProblemGetByIdResult>(null) {
        setUpdater {
            ProblemModel.getById(id)
        }
        action(this@observableOf)
    }
}