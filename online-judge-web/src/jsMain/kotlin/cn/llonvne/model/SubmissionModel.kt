package cn.llonvne.model

import cn.llonvne.kvision.service.ISubmissionService
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
}