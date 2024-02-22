package cn.llonvne.database.aware

import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.entity.problem.context.Problem
import org.springframework.stereotype.Service

@Service
class ProblemAwareProvider(
    private val problemRepository: ProblemRepository
) {

    suspend fun <R> awareOf(
        problem: Problem,
        action: suspend context(ProblemAwarer) () -> R
    ): R {
        val problemAwarer = ProblemAwarer(problem = problem)

        return action(problemAwarer)
    }

    inner class ProblemAwarer(
        val problem: Problem
    )
}