@file:UseContextualSerialization

package cn.llonvne.kvision.service

import cn.llonvne.dtos.AuthenticationUserDto
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemTestCases
import cn.llonvne.entity.problem.context.SubmissionTestCases
import cn.llonvne.entity.problem.context.passer.PasserResult
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
    data object SubmissionNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp, PlaygroundOutput

    @Serializable
    data object ProblemNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp, GetSupportLanguageByProblemIdResp,
        ProblemSubmissionResp, GetLastNProblemSubmissionResp, IContestService.AddProblemResp

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
    sealed interface GetJudgeResultByCodeIdResp

    @Serializable
    sealed interface ProblemOutput : GetJudgeResultByCodeIdResp {
        @Serializable
        data class SuccessProblemOutput(
            val problem: ProblemJudgeResult
        ) : PlaygroundOutput
    }

    @Serializable
    sealed interface PlaygroundOutput : GetJudgeResultByCodeIdResp {
        @Serializable
        data class SuccessPlaygroundOutput(
            val outputDto: OutputDto
        ) : PlaygroundOutput

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
    ): GetJudgeResultByCodeIdResp

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
        val visibilityType: SubmissionVisibilityType,
        val contestId: ContestId? = null
    )

    @Serializable
    data class ProblemSubmissionRespNotPersist(
        val problemTestCases: ProblemTestCases,
        val submissionTestCases: SubmissionTestCases,
    )

    @Serializable
    sealed interface ProblemSubmissionResp {
        @Serializable
        data class ProblemSubmissionRespImpl(
            val codeId: Int,
            val problemTestCases: ProblemTestCases,
            val submissionTestCases: SubmissionTestCases,
        ) : ProblemSubmissionResp
    }

    suspend fun submit(
        value: AuthenticationToken?,
        submissionSubmit: ProblemSubmissionReq
    ): ProblemSubmissionResp

    @Serializable
    sealed interface GetLastNProblemSubmissionResp {
        @Serializable
        data class GetLastNProblemSubmissionRespImpl(
            val submissions: List<ProblemSubmissionListDto>
        ) : GetLastNProblemSubmissionResp

        @Serializable
        data class ProblemSubmissionListDto(
            val language: Language,
            val submitTime: LocalDateTime,
            val codeId: Int,
            val passerResult: PasserResult,
        )
    }

    suspend fun getLastNProblemSubmission(
        value: AuthenticationToken?,
        problemId: Int,
        lastN: Int
    ): GetLastNProblemSubmissionResp
}