package cn.llonvne.model

import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.entity.problem.Submission
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
}