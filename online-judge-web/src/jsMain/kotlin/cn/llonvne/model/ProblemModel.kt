package cn.llonvne.model

import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.context.ProblemContext
import cn.llonvne.entity.problem.context.ProblemTestCases
import cn.llonvne.entity.problem.context.ProblemType
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.fromId
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.kvision.service.IProblemService.CreateProblemReq
import cn.llonvne.kvision.service.IProblemService.CreateProblemResp
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Message
import cn.llonvne.message.Message.ToastMessage
import cn.llonvne.message.MessageLevel
import cn.llonvne.message.Messager
import cn.llonvne.site.problem.CreateProblemForm
import io.kvision.remote.getService

object ProblemModel {
    private val problemService = getService<IProblemService>()

    suspend fun listProblem() = problemService.list(AuthenticationModel.userToken.value)

    suspend fun create(problemForm: CreateProblemForm, testCases: ProblemTestCases): CreateProblemResp {

        if (AuthenticationModel.userToken.value == null) {
            Messager.send(ToastMessage(MessageLevel.Warning, "必须要登入才能发送消息哦"))
            return PermissionDenied
        }

        return problemService.create(
            AuthenticationModel.userToken.value,
            CreateProblemReq(
                problemForm.problemName,
                problemForm.problemDescription,
                ProblemContext(
                    inputDescription = problemForm.inputDescr,
                    outputDescription = problemForm.outputDescr,
                    hint = problemForm.hint,
                    testCases = testCases,
                    overall = problemForm.overall
                ),
                authorId = problemForm.authorId,
                memoryLimit = problemForm.memoryLimit,
                timeLimit = problemForm.timeLimit,
                visibility = problemForm.problemVisibilityInt.let {
                    ProblemVisibility.entries[it.toInt()]
                },
                type = problemForm.problemTypeInt.let {
                    ProblemType.entries[it.toInt()]
                },
                supportLanguages = problemForm.problemSupportLanguages.split(",")
                    .map { it.toInt() }.mapNotNull {
                        SupportLanguages.fromId(it)
                    }.map {
                        Language(
                            languageId = it.languageId,
                            languageName = it.languageName,
                            languageVersion = it.languageVersion
                        )
                    }
            )
        )
    }

    suspend fun getById(id: Int) = problemService.getById(id)

    suspend fun search(text: String): List<ProblemListDto> {
        return problemService.search(AuthenticationModel.userToken.value, text)
    }
}