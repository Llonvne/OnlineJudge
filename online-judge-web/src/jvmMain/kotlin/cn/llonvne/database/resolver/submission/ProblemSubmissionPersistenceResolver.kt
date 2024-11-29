package cn.llonvne.database.resolver.submission

import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.database.resolver.contest.ContestIdGetResolver
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.entity.problem.SubmissionVisibilityType.*
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionReq
import org.springframework.stereotype.Service

@Service
class ProblemSubmissionPersistenceResolver(
    private val codeRepository: CodeRepository,
    private val submissionRepository: SubmissionRepository,
    private val contestIdGetResolver: ContestIdGetResolver,
) {
    sealed interface ProblemSubmissionPersistenceResult {
        data class Success(
            val submissionId: Int,
            val codeId: Int,
        ) : ProblemSubmissionPersistenceResult

        data object NotNeedToPersist : ProblemSubmissionPersistenceResult

        data class Failed(
            val reason: String,
        ) : ProblemSubmissionPersistenceResult
    }

    suspend fun resolve(
        user: AuthenticationUser,
        result: ISubmissionService.ProblemSubmissionRespNotPersist,
        problemSubmissionReq: ProblemSubmissionReq,
        language: SupportLanguages,
    ): ProblemSubmissionPersistenceResult {
        if (problemSubmissionReq.contestId != null) {
            return persistAsContestSubmission(
                user,
                result,
                problemSubmissionReq,
                language,
                contestId = problemSubmissionReq.contestId,
            )
        }

        return persist(user, result, problemSubmissionReq, language)
    }

    private suspend fun persistAsContestSubmission(
        user: AuthenticationUser,
        result: ISubmissionService.ProblemSubmissionRespNotPersist,
        problemSubmissionReq: ProblemSubmissionReq,
        language: SupportLanguages,
        contestId: ContestId,
    ): ProblemSubmissionPersistenceResult {
        val code =
            codeRepository.save(
                Code(
                    codeId = null,
                    authenticationUserId = user.id,
                    code = problemSubmissionReq.code,
                    codeType = Code.CodeType.Problem,
                    languageId = problemSubmissionReq.languageId,
                    visibilityType = CodeVisibilityType.Restrict,
                    commentType = CodeCommentType.ContestCode,
                ),
            )

        if (code.codeId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Code 在持久化后 id 仍然为空·")
        }

        val passerResult = result.problemTestCases.passer.pass(result.submissionTestCases)

        val submission =
            submissionRepository.save(
                Submission(
                    submissionId = null,
                    authenticationUserId = user.id,
                    codeId = code.codeId,
                    judgeResult =
                        ProblemJudgeResult(
                            result.problemTestCases,
                            result.submissionTestCases,
                            passerResult = passerResult,
                        ).json(),
                    problemId = problemSubmissionReq.problemId,
                    status = SubmissionStatus.Finished,
                    contestId =
                        contestIdGetResolver.resolve(contestId)?.contestId
                            ?: return ProblemSubmissionPersistenceResult.Failed("无效的 ContestId"),
                ),
            )

        if (submission.submissionId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Submission 在持久后 id 仍然为空")
        }

        return ProblemSubmissionPersistenceResult.Success(
            submissionId = submission.submissionId,
            codeId = code.codeId,
        )
    }

    private suspend fun persist(
        user: AuthenticationUser,
        result: ISubmissionService.ProblemSubmissionRespNotPersist,
        problemSubmissionReq: ProblemSubmissionReq,
        language: SupportLanguages,
    ): ProblemSubmissionPersistenceResult {
        val code =
            codeRepository.save(
                Code(
                    codeId = null,
                    authenticationUserId = user.id,
                    code = problemSubmissionReq.code,
                    codeType = Code.CodeType.Problem,
                    languageId = problemSubmissionReq.languageId,
                    visibilityType =
                        when (problemSubmissionReq.visibilityType) {
                            PUBLIC -> CodeVisibilityType.Public
                            PRIVATE -> CodeVisibilityType.Private
                            Contest -> CodeVisibilityType.Restrict
                        },
                ),
            )

        if (code.codeId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Code 在持久化后 id 仍然为空·")
        }

        val passerResult = result.problemTestCases.passer.pass(result.submissionTestCases)

        val submission =
            submissionRepository.save(
                Submission(
                    submissionId = null,
                    authenticationUserId = user.id,
                    codeId = code.codeId,
                    judgeResult =
                        ProblemJudgeResult(
                            result.problemTestCases,
                            result.submissionTestCases,
                            passerResult = passerResult,
                        ).json(),
                    problemId = problemSubmissionReq.problemId,
                    status = SubmissionStatus.Finished,
                    contestId =
                        if (problemSubmissionReq.contestId == null) {
                            null
                        } else {
                            contestIdGetResolver.resolve(problemSubmissionReq.contestId)?.contestId
                        },
                ),
            )

        if (submission.submissionId == null) {
            return ProblemSubmissionPersistenceResult.Failed("Submission 在持久后 id 仍然为空")
        }

        return ProblemSubmissionPersistenceResult.Success(
            submissionId = submission.submissionId,
            codeId = code.codeId,
        )
    }
}
