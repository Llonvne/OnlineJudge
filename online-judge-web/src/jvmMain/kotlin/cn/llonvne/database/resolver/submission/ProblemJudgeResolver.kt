package cn.llonvne.database.resolver.submission

import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.SubmissionTestCases
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp
import cn.llonvne.kvision.service.JudgeService
import org.springframework.stereotype.Service

/**
 * 执行判题程序，不做权限判断
 */
@Service
class ProblemJudgeResolver(
    private val judgeService: JudgeService
) {
    suspend fun resolve(
        problem: Problem,
        submissionSubmit: ProblemSubmissionReq,
        language: SupportLanguages
    ): ProblemSubmissionResp {
        val results = problem.context.testCases.canJudge().map { testcase ->
            testcase to judgeService.judge(
                language,
                testcase.input,
                testcase.expect,
            )
        }.map { (problemTestcase, output) ->
            SubmissionTestCases.SubmissionTestCase.from(problemTestcase, output)
        }

        TODO()
    }
}