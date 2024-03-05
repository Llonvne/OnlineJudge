package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.loading
import cn.llonvne.compoent.submission.SubmitProblemResolver
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.IContestService
import cn.llonvne.site.contest.Display
import cn.llonvne.site.problem.detail.CodeEditorShower
import cn.llonvne.site.problem.detail.detail
import io.kvision.core.Container
import io.kvision.html.div

interface ContestProblemDisplay {

    companion object {
        fun from(contestId: ContestId): ContestProblemDisplay = BaseContestProblemDisplay(contestId)
    }

    fun show(container: Container, index: Display.ProblemIndex)
}

private class BaseContestProblemDisplay(private val contestId: ContestId) : ContestProblemDisplay {
    override fun show(container: Container, index: Display.ProblemIndex) {
        container.div {
            detail(div { }, index.id) {
                disableHistory = true
                submitProblemResolver = SubmitProblemResolver(contestId = contestId)
                codeEditorConfigurer = CodeEditorShower.CodeEditorConfigurer {
                    forceVisibility = SubmissionVisibilityType.Contest
                }
            }
        }
    }
}