package cn.llonvne.database.resolver.submission

import cn.llonvne.database.aware.ProblemAwareProvider.ProblemAwarer
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemVisibility.*
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import org.springframework.stereotype.Service

@Service
class IndividualProblemPassResolver(
) {
    context(ProblemAwarer)
    suspend fun resolve(
        authenticationUser: AuthenticationUser,
        problemSubmissionReq: ISubmissionService.ProblemSubmissionReq,
        pass: suspend (Problem) -> ProblemSubmissionResp
    ): ProblemSubmissionResp {
        return when (problem.visibility) {
            Public -> {
                pass(problem)
            }

            Private -> {
                if (problem.ownerId == authenticationUser.id) {
                    pass(problem)
                } else {
                    PermissionDeniedWithMessage("你无法提交该题目，应该该题目为私有")
                }
            }

            Restrict -> {
                TODO()
            }
        }
    }
}