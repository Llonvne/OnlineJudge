package cn.llonvne.compoent.submission

import cn.llonvne.dtos.PlaygroudSubmission
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel


class SubmitProblemResolver(
    private val contestId: ContestId? = null
) {

    suspend fun resolve(problemId: Int, data: PlaygroudSubmission) {
        val submissionVisibilityType = data.visibilityTypeStr.let {
            SubmissionVisibilityType.entries[it?.toIntOrNull() ?: return Messager.toastInfo("提交可见性ID无效")]
        }
        SubmissionModel.submit(
            ISubmissionService.ProblemSubmissionReq(
                problemId = problemId,
                code = data.code ?: return Messager.toastInfo("代码不可以为空"),
                visibilityType = submissionVisibilityType,
                languageId = data.languageId?.toIntOrNull() ?: return Messager.toastInfo("语言无效或为空"),
                contestId = contestId
            )
        ) {
            val passerResult = it.problemTestCases.passer.pass(it.submissionTestCases)
            PasserResultShower.from(passerResult, codeId = it.codeId).show()
        }
    }
}