package cn.llonvne.database.resolver.submission

import cn.llonvne.database.aware.ProblemAwareProvider
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemType.Group
import cn.llonvne.entity.problem.context.ProblemType.Individual
import cn.llonvne.kvision.service.ISubmissionService.*
import org.springframework.stereotype.Service

@Service
class ProblemSubmissionPassResolver(
    private val problemRepository: ProblemRepository,
    private val groupProblemPassResolver: GroupProblemPassResolver,
    private val individualProblemPassResolver: IndividualProblemPassResolver,
    private val problemAwareProvider: ProblemAwareProvider
) {
    suspend fun resolve(
        authenticationUser: AuthenticationUser,
        problemSubmissionReq: ProblemSubmissionReq,
        onPass: suspend (Problem) -> ProblemSubmissionResp
    ): ProblemSubmissionResp {
        val problem = problemRepository.getById(problemSubmissionReq.problemId) ?: return ProblemNotFound

        // 如果是以 Contest 形式提交,则无需检查群组/个人权限
        if (problemSubmissionReq.contestId != null) {
            return onPass(problem)
        }

        // 否则检查权限
        return problemAwareProvider.awareOf(problem) {
            when (problem.type) {
                Individual -> individualProblemPassResolver.resolve(
                    authenticationUser, problemSubmissionReq, onPass
                )

                Group -> groupProblemPassResolver.resolve(
                    authenticationUser, problemSubmissionReq, onPass
                )
            }
        }
    }
}