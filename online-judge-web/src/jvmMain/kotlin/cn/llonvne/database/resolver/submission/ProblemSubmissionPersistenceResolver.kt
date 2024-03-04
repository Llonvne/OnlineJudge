package cn.llonvne.database.resolver.submission

import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.dtos.SubmissionSubmit
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.JudgeResult
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp.ProblemSubmissionRespImpl
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import org.springframework.stereotype.Service

@Service
class ProblemSubmissionPersistenceResolver(
    private val codeRepository: CodeRepository,
    private val submissionRepository: SubmissionRepository
) {

    sealed interface ProblemSubmissionPersistenceResult {
        data class Success(val submissionId: Int, val codeId: Int) : ProblemSubmissionPersistenceResult

        data object NotNeedToPersist : ProblemSubmissionPersistenceResult

        data class Failed(val reason: String) : ProblemSubmissionPersistenceResult
    }

    suspend fun resolve(
        user: AuthenticationUser,
        result: ISubmissionService.ProblemSubmissionRespNotPersist,
        submissionSubmit: ProblemSubmissionReq,
        language: SupportLanguages
    ): ProblemSubmissionPersistenceResult {
        return persist(user, result, submissionSubmit, language)
    }

    private suspend fun persist(
        user: AuthenticationUser,
        result: ISubmissionService.ProblemSubmissionRespNotPersist,
        submissionSubmit: ProblemSubmissionReq,
        language: SupportLanguages
    ): ProblemSubmissionPersistenceResult {
        val code = codeRepository.save(
            Code(
                codeId = null,
                authenticationUserId = user.id,
                code = submissionSubmit.code,
                codeType = Code.CodeType.Problem,
                languageId = submissionSubmit.languageId
            )
        )

        if (code.codeId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Code 在持久化后 id 仍然为空·")
        }

        val passerResult = result.problemTestCases.passer.pass(result.submissionTestCases)

        val submission = submissionRepository.save(
            Submission(
                submissionId = null,
                authenticationUserId = user.id,
                codeId = code.codeId,
                judgeResult = ProblemJudgeResult(
                    result.problemTestCases,
                    result.submissionTestCases,
                    passerResult = passerResult
                ).json(),
                problemId = submissionSubmit.problemId
            )
        )

        if (submission.submissionId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Submission 在持久后 id 仍然为空")
        }

        return ProblemSubmissionPersistenceResult.Success(
            submissionId = submission.submissionId,
            codeId = code.codeId
        )
    }
}