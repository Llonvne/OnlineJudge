package cn.llonvne.model

import cn.llonvne.entity.problem.context.ProblemContext
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.site.CreateProblemForm
import io.kvision.remote.getService

object ProblemModel {
    private val problemService = getService<IProblemService>()

    suspend fun listProblem() = problemService.list(AuthenticationModel.userToken.value)

    suspend fun create(problemForm: CreateProblemForm): IProblemService.CreateProblemResp {
        return problemService.create(
            AuthenticationModel.userToken.value,
            IProblemService.CreateProblemReq(
                problemForm.problemName,
                problemForm.problemDescription,
                ProblemContext(context = ""),
                authorId = problemForm.authorId,
                memoryLimit = problemForm.memoryLimit,
                timeLimit = problemForm.timeLimit
            )
        )
    }

    suspend fun getById(id: Int) = problemService.getById(id)
}