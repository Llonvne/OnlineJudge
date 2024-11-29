package cn.llonvne.database.resolver.submission

import cn.llonvne.database.aware.ProblemAwareProvider.ProblemAwarer
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemVisibility.*
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import org.springframework.stereotype.Service

@Service
class GroupProblemPassResolver {
    context(ProblemAwarer)
    suspend fun resolve(
        authenticationUser: AuthenticationUser,
        problemSubmissionReq: ISubmissionService.ProblemSubmissionReq,
        onPass: suspend (Problem) -> ISubmissionService.ProblemSubmissionResp,
    ): ISubmissionService.ProblemSubmissionResp {
        return when (problem.visibility) {
            Public -> onPass(problem)
            Private -> {
                return if (problem.ownerId == authenticationUser.id) {
                    onPass(problem)
                } else {
                    PermissionDeniedWithMessage("你无权提交该题目")
                }
            }

            Restrict -> {
                TODO()
            }
        }
    }
}
