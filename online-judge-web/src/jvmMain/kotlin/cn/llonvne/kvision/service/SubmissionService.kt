package cn.llonvne.kvision.service

import cn.llonvne.database.repository.*
import cn.llonvne.dtos.AuthenticationUserDto
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeVisibilityType.*
import cn.llonvne.exts.now
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.fromId
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.api.task.Output.Companion.formatOnSuccess
import cn.llonvne.gojudge.api.task.format
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.CreateSubmissionReq.PlaygroundCreateSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.*
import cn.llonvne.kvision.service.ISubmissionService.SubmissionGetByIdResp.SuccessfulGetById
import cn.llonvne.security.AuthenticationToken
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val languageRepository: LanguageRepository,
    private val problemRepository: ProblemRepository,
    private val authenticationUserRepository: AuthenticationUserRepository,
    private val judgeService: JudgeService,
    private val codeRepository: CodeRepository
) : ISubmissionService {

    private val json = Json

    override suspend fun list(req: ListSubmissionReq): List<SubmissionListDto> {

        return submissionRepository.list()
            .mapNotNull {
                when (val result = it.toSubmissionListDto()) {
                    is SuccessfulGetById -> result.submissionListDto
                    else -> null
                }
            }
    }

    override suspend fun getById(id: Int): SubmissionGetByIdResp {
        val submission =
            submissionRepository.getById(id) ?: return SubmissionNotFound

        return submission.toSubmissionListDto()
    }

    override suspend fun getViewCode(id: Int): ViewCodeGetByIdResp {
        val submission = submissionRepository.getById(id) ?: return SubmissionNotFound

        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(submission.judgeResult)
        }

        val problem =
            problemRepository.getById(submission.problemId ?: return ProblemNotFound) ?: return ProblemNotFound

        val code = codeRepository.get(
            submission.codeId
        ) ?: return CodeNotFound

        return ViewCodeGetByIdResp.SuccessfulGetById(
            ViewCodeDto(
                rawCode = code.code,
                language = languageRepository.getByIdOrNull(code.languageId)
                    ?: return LanguageNotFound,
                problemName = problem.problemName,
                problemId = problem.problemId ?: return ProblemNotFound,
                output = result.getOrNull(),
                status = submission.status,
                submissionId = submission.submissionId ?: return SubmissionNotFound
            )
        )
    }

    private suspend fun Submission.toSubmissionListDto(): SubmissionGetByIdResp {
        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(judgeResult)
        }

        val problem = problemRepository.getById(problemId ?: return ProblemNotFound) ?: return ProblemNotFound
        val languageId = codeRepository.getCodeLanguageId(this.codeId)
        return SuccessfulGetById(
            SubmissionListDto(
                language = languageRepository.getByIdOrNull(languageId) ?: return LanguageNotFound,
                user = AuthenticationUserDto(
                    authenticationUserRepository.getByIdOrNull(authenticationUserId)?.username
                        ?: return UserNotFound,
                ),
                problemId = problemId,
                problemName = problem.problemName,
                submissionId = this.submissionId ?: return SubmissionNotFound,
                status = this.status,
                runTime = result.getOrNull().format("-") {
                    formatOnSuccess("-") {
                        it.runResult.runTime.toString()
                    }
                },
                runMemory = result.getOrNull().format("-") {
                    formatOnSuccess("-") {
                        it.runResult.memory.toString()
                    }
                },
                codeLength = codeRepository.getCodeLength(codeId)?.toLong() ?: return CodeNotFound,
                submitTime = this.createdAt ?: LocalDateTime.now()
            )
        )
    }

    override suspend fun getSupportLanguageId(
        authenticationToken: AuthenticationToken?,
        problemId: Int
    ): GetSupportLanguageByProblemIdResp {
        if (!problemRepository.isIdExist(problemId)) {
            return ProblemNotFound
        }

        return GetSupportLanguageByProblemIdResp.SuccessfulGetSupportLanguage(
            problemRepository.getSupportLanguage(problemId)
        )
    }

    override suspend fun create(
        authenticationToken: AuthenticationToken?,
        createSubmissionReq: CreateSubmissionReq
    ): CreateSubmissionResp {

        if (authenticationToken == null) {
            return PermissionDenied
        }

        val language = SupportLanguages.fromId(createSubmissionReq.languageId) ?: return LanguageNotFound

        val code = codeRepository.save(
            Code(
                authenticationUserId = authenticationToken.authenticationUserId,
                code = createSubmissionReq.rawCode,
                languageId = createSubmissionReq.languageId,
                visibilityType = Private,
                codeType = createSubmissionReq.codeType
            )
        )

        return when (createSubmissionReq) {
            is PlaygroundCreateSubmissionReq -> onPlaygroundCreateSubmission(
                code, language, createSubmissionReq
            )
        }
    }

    override suspend fun getOutputByCodeId(
        authenticationToken: AuthenticationToken?,
        codeId: Int
    ): GetOutputByCodeIdResp {

        val visibilityType = codeRepository.getCodeVisibilityType(codeId) ?: return CodeNotFound
        val codeOwnerId = codeRepository.getCodeOwnerId(codeId) ?: return CodeNotFound

        when (visibilityType) {
            Public -> {
            }

            Private -> {
                if (authenticationToken?.authenticationUserId != codeOwnerId) {
                    return PermissionDenied
                }
            }

            Restrict -> {

            }
        }

        val submission = submissionRepository.getByCodeId(codeId) ?: return SubmissionNotFound

        val output = kotlin.runCatching {
            json.decodeFromString<Output>(submission.judgeResult)
        }

        output.onFailure {
            return JudgeResultParseError
        }.onSuccess {
            return GetOutputByCodeIdResp.SuccessGetOutput(it)
        }
        TODO()
    }

    override suspend fun getLastNPlaygroundSubmission(
        authenticationToken: AuthenticationToken?,
        last: Int
    ): GetLastNPlaygroundSubmissionResp {
        if (authenticationToken == null) {
            return PermissionDenied
        }
        return submissionRepository.getByAuthenticationUserID(
            authenticationToken.authenticationUserId,
            Code.CodeType.Playground,
            last
        ).map { sub ->

            val languageId = codeRepository.getCodeLanguageId(sub.codeId) ?: return LanguageNotFound
            val language = languageRepository.getByIdOrNull(languageId) ?: return LanguageNotFound

            PlaygroundSubmissionDto(
                language = language,
                codeId = sub.codeId,
                status = sub.status,
                submissionId = sub.submissionId ?: return InternalError("Submission 被查询出来，但不存在 Id"),
                submitTime = sub.createdAt ?: return InternalError("Submission 被查询出来，但不存在创建时间"),
                user = AuthenticationUserDto(
                    username = authenticationUserRepository.getByIdOrNull(sub.authenticationUserId)?.username
                        ?: return InternalError("Submission 被查询出来，但不存在 User")
                )
            )
        }.let {
            SuccessGetLastNPlaygroundSubmission(it)
        }
    }

    private suspend fun onPlaygroundCreateSubmission(
        code: Code,
        language: SupportLanguages,
        req: PlaygroundCreateSubmissionReq
    ): CreateSubmissionResp {

        val output = runCatching {
            judgeService.judge(
                languages = language,
                code = code.code,
                stdin = req.stdin
            )
        }.onFailure {
            return JudgeError(it)
        }

        val judgeResult = json.encodeToString(output.getOrNull())

        val submission = submissionRepository.save(
            Submission(
                authenticationUserId = code.authenticationUserId,
                judgeResult = judgeResult,
                problemId = null,
                codeId = code.codeId ?: return InternalError("CodeId 在插入后仍不存在"),
                status = SubmissionStatus.Finished
            )
        )

        return CreateSubmissionResp.SuccessfulCreateSubmissionResp(
            submission.submissionId
                ?: return InternalError("插入 Submission 后 SubmissionId 仍不存在"),
            code.codeId
        )
    }
}