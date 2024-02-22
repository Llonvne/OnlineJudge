@file:UseContextualSerialization

package cn.llonvne.kvision.service

import cn.llonvne.dtos.AuthenticationUserDto
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization


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
    sealed interface GetSupportLanguageByProblemIdResp {
        @Serializable
        data class SuccessfulGetSupportLanguage(val languages: List<Language>) : GetSupportLanguageByProblemIdResp
    }

    suspend fun getSupportLanguageId(
        authenticationToken: AuthenticationToken?, problemId: Int
    ): GetSupportLanguageByProblemIdResp

    @Serializable
    data object SubmissionNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp, GetOutputByCodeIdResp

    @Serializable
    data object ProblemNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp, GetSupportLanguageByProblemIdResp,
        ProblemSubmissionResp

    @Serializable
    data object UserNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp

    @Serializable
    sealed interface CreateSubmissionReq {
        val rawCode: String
        val languageId: Int
        val codeType: Code.CodeType

        @Serializable
        data class PlaygroundCreateSubmissionReq(
            val stdin: String,
            override val rawCode: String,
            override val languageId: Int,
            override val codeType: Code.CodeType
        ) : CreateSubmissionReq
    }

    @Serializable
    sealed interface CreateSubmissionResp {

        @Serializable
        data class SuccessfulCreateSubmissionResp(val submissionId: Int, val codeId: Int) : CreateSubmissionResp
    }

    suspend fun create(
        authenticationToken: AuthenticationToken?, createSubmissionReq: CreateSubmissionReq
    ): CreateSubmissionResp

    @Serializable
    sealed interface GetOutputByCodeIdResp {
        @Serializable
        data class SuccessGetOutput(
            val outputDto: OutputDto
        ) : GetOutputByCodeIdResp

        @Serializable
        sealed interface OutputDto {
            val language: SupportLanguages

            @Serializable
            data class SuccessOutput(
                val stdin: String,
                val stdout: String,
                override val language: SupportLanguages,
            ) : OutputDto

            @Serializable
            sealed interface FailureReason {
                @Serializable
                data object CompileResultNotFound : FailureReason

                @Serializable
                data class CompileError(val compileErrMessage: String) : FailureReason

                @Serializable
                data object RunResultIsNull : FailureReason

                @Serializable
                data object TargetResultNotFound : FailureReason
            }

            @Serializable
            data class FailureOutput(
                val reason: FailureReason,
                override val language: SupportLanguages
            ) : OutputDto
        }
    }

    suspend fun getOutputByCodeId(
        authenticationToken: AuthenticationToken?, codeId: Int
    ): GetOutputByCodeIdResp

    @Serializable
    sealed interface GetLastNPlaygroundSubmissionResp {
        @Serializable
        data class PlaygroundSubmissionDto(
            val language: Language,
            val user: AuthenticationUserDto,
            val submissionId: Int,
            val status: SubmissionStatus,
            val submitTime: LocalDateTime,
            val codeId: Int
        )

        @Serializable
        data class SuccessGetLastNPlaygroundSubmission(
            val subs: List<PlaygroundSubmissionDto>
        ) : GetLastNPlaygroundSubmissionResp
    }

    suspend fun getLastNPlaygroundSubmission(
        authenticationToken: AuthenticationToken?, last: Int = 5
    ): GetLastNPlaygroundSubmissionResp

    @Serializable
    data class ProblemSubmissionReq(
        val code: String,
        val problemId: Int,
        val languageId: Int,
        val visibilityType: SubmissionVisibilityType
    )

    @Serializable
    sealed interface ProblemSubmissionResp {

    }

    suspend fun submit(
        value: AuthenticationToken?,
        submissionSubmit: ProblemSubmissionReq
    ): ProblemSubmissionResp
}