package cn.llonvne.model

import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.context.ProblemContext
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Message
import cn.llonvne.message.MessageLevel
import cn.llonvne.message.Messager
import cn.llonvne.site.CreateProblemForm
import io.kvision.remote.getService

object ProblemModel {
    private val problemService = getService<IProblemService>()

    suspend fun listProblem() = problemService.list(AuthenticationModel.userToken.value)

    suspend fun create(problemForm: CreateProblemForm): IProblemService.CreateProblemResp {

        if (AuthenticationModel.userToken.value == null) {
            Messager.send(Message.ToastMessage(MessageLevel.Warning, "必须要登入才能发送消息哦"))
            return PermissionDenied
        }

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

    suspend fun search(text: String): List<ProblemListDto> {
        return problemService.search(AuthenticationModel.userToken.value, text)
    }
}