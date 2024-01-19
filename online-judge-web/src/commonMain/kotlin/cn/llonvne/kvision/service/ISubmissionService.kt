package cn.llonvne.kvision.service

import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.Submission
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface ISubmissionService {

    @Serializable
    data class ListSubmissionReq(
        val token: AuthenticationToken? = null,
    )

    suspend fun list(req: ListSubmissionReq): List<SubmissionListDto>

    @Serializable
    sealed interface SubmissionGetByIdResp {
        @Serializable
        data class SuccessfulGetById(val submissionListDto: SubmissionListDto) : SubmissionGetByIdResp
    }

    suspend fun getById(id: Int): SubmissionGetByIdResp

    @Serializable
    sealed interface ViewCodeGetByIdResp {
        @Serializable
        data class SuccessfulGetById(val viewCodeDto: ViewCodeDto) : ViewCodeGetByIdResp
    }


    suspend fun getViewCode(id: Int): ViewCodeGetByIdResp

    @Serializable
    data object SubmissionNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp

    @Serializable
    data object LanguageNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp

    @Serializable
    data object ProblemNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp

    @Serializable
    data object UserNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp
}