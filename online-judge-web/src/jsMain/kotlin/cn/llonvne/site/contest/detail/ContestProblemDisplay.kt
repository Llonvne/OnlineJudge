package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.submission.SubmitProblemResolver
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.site.contest.Display
import cn.llonvne.site.problem.detail.CodeEditorShower
import cn.llonvne.site.problem.detail.detail
import io.kvision.core.Container
import io.kvision.html.div

interface ContestProblemDisplay {
    companion object {
        fun from(
            contestId: ContestId,
            statusResolver: ContestStatusResolver,
        ): ContestProblemDisplay = BaseContestProblemDisplay(contestId, statusResolver)
    }

    fun show(
        container: Container,
        index: Display.ProblemIndex,
    )
}

private class BaseContestProblemDisplay(
    private val contestId: ContestId,
    private val statusResolver: ContestStatusResolver,
) : ContestProblemDisplay {
    override fun show(
        container: Container,
        index: Display.ProblemIndex,
    ) {
        container.div {
            detail(div { }, index.id) {
                notShowProblem =
                    statusResolver.status() == Contest.ContestStatus.NotBegin
                notShowProblemMessage = "比赛还未开始"
                disableHistory = true
                submitProblemResolver = SubmitProblemResolver(contestId = contestId)
                codeEditorConfigurer =
                    CodeEditorShower.CodeEditorConfigurer {
                        forceVisibility = SubmissionVisibilityType.Contest
                        submitProblemResolver = SubmitProblemResolver(contestId)
                        showSubmitPanel = statusResolver.status() == Contest.ContestStatus.Running
                        notShowSubmitPanelMessage = "比赛尚未开始或者已经结束"
                    }
            }
        }
    }
}
