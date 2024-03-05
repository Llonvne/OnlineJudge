package cn.llonvne.database.resolver.contest

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.entity.problem.context.ProblemVisibility.*
import org.springframework.stereotype.Service

@Service
class ContestProblemVisibilityCheckResolver {

    enum class ContestProblemVisibilityCheckResult {
        Pass,
        Reject
    }

    fun check(authenticationUser: AuthenticationUser, problem: Problem): ContestProblemVisibilityCheckResult {
        return when (problem.visibility) {
            Public -> ContestProblemVisibilityCheckResult.Pass
            Private -> {
                if (authenticationUser.id == problem.ownerId) {
                    ContestProblemVisibilityCheckResult.Pass
                } else {
                    ContestProblemVisibilityCheckResult.Reject
                }
            }

            Restrict -> {
                ContestProblemVisibilityCheckResult.Reject
            }
        }
    }
}