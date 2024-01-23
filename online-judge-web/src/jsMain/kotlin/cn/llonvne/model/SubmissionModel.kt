package cn.llonvne.model

import cn.llonvne.entity.problem.share.Code
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.site.PlaygroundSubmission
import io.kvision.remote.getService

object SubmissionModel {
    private val submissionService = getService<ISubmissionService>()

    suspend fun list() = submissionService.list(
        ISubmissionService.ListSubmissionReq(
            AuthenticationModel.userToken.value
        )
    )

    suspend fun getById(id: Int): ISubmissionService.SubmissionGetByIdResp = submissionService.getById(id)

    suspend fun codeGetById(id: Int) = submissionService.getViewCode(id)

    suspend fun getSupportLanguage(problemId: Int) =
        submissionService.getSupportLanguageId(AuthenticationModel.userToken.value, problemId)

    suspend fun submit(playgroundSubmission: PlaygroundSubmission): ISubmissionService.CreateSubmissionResp {
        return submissionService.create(
            AuthenticationModel.userToken.value,
            ISubmissionService.CreateSubmissionReq.PlaygroundCreateSubmissionReq(
                languageId = playgroundSubmission.language.toInt(),
                rawCode = playgroundSubmission.code,
                stdin = playgroundSubmission.stdin ?: "",
                codeType = Code.CodeType.Playground
            )
        )
    }

    suspend fun getJudgeResultByCodeID(codeId: Int) = submissionService.getOutputByCodeId(
        AuthenticationModel.userToken.value, codeId
    )

    suspend fun getLastNPlaygroundSubmission(lastN: Int = 5) =
        submissionService.getLastNPlaygroundSubmission(
            AuthenticationModel.userToken.value, lastN
        )
}