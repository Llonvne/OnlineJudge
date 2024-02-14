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
import cn.llonvne.gojudge.api.spec.runtime.GoJudgeFile
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.api.task.Output.Companion.memoryRepr
import cn.llonvne.gojudge.api.task.Output.Companion.runTimeRepr
import cn.llonvne.gojudge.api.task.Output.Failure
import cn.llonvne.gojudge.api.task.Output.Failure.CompileResultIsNull
import cn.llonvne.gojudge.api.task.Output.Failure.TargetFileNotExist
import cn.llonvne.gojudge.api.task.Output.Success
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.CreateSubmissionReq.PlaygroundCreateSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.PlaygroundSubmissionDto
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.SuccessGetLastNPlaygroundSubmission
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto.FailureOutput
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto.FailureReason.*
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto.SuccessOutput
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.SuccessGetOutput
import cn.llonvne.kvision.service.ISubmissionService.GetSupportLanguageByProblemIdResp.SuccessfulGetSupportLanguage
import cn.llonvne.kvision.service.ISubmissionService.SubmissionGetByIdResp.SuccessfulGetById
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val codeRepository: CodeRepository,
    private val authentication: RedisAuthenticationService
) : ISubmissionService {

    private val json = Json

    override suspend fun list(req: ListSubmissionReq): List<SubmissionListDto> {
        return submissionRepository.list()
            .mapNotNull { submission ->
                when (val result = submission.aslistDto()) {
                    is SuccessfulGetById -> result.submissionListDto
                    else -> null
                }
            }
    }

    override suspend fun getById(id: Int): SubmissionGetByIdResp {
        val submission =
            submissionRepository.getById(id) ?: return SubmissionNotFound

        return submission.aslistDto()
    }

    override suspend fun getViewCode(id: Int): ViewCodeGetByIdResp {
        val submission = submissionRepository.getById(id) ?: return SubmissionNotFound

        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(submission.judgeResult)
        }

        val problem =
            problemRepository.getById(submission.problemId) ?: return ProblemNotFound

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

    private suspend fun Submission.aslistDto(): SubmissionGetByIdResp {
        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(judgeResult)
        }

        val problem = problemRepository.getById(problemId) ?: return ProblemNotFound

        return problem.onIdNotNull(ProblemNotFound) { problemId, problem ->
            val languageId = codeRepository.getCodeLanguageId(codeId)
            SuccessfulGetById(
                SubmissionListDto(
                    language = languageRepository.getByIdOrNull(languageId) ?: return@onIdNotNull LanguageNotFound,
                    user = AuthenticationUserDto(
                        authenticationUserRepository.getByIdOrNull(authenticationUserId)?.username
                            ?: return@onIdNotNull UserNotFound,
                    ),
                    problemId = problemId,
                    problemName = problem.problemName,
                    submissionId = this.submissionId ?: return@onIdNotNull SubmissionNotFound,
                    status = this.status,
                    runTime = result.getOrNull().runTimeRepr,
                    runMemory = result.getOrNull().memoryRepr,
                    codeLength = codeRepository.getCodeLength(codeId)?.toLong() ?: return@onIdNotNull CodeNotFound,
                    submitTime = this.createdAt ?: LocalDateTime.now()
                )
            )
        }
    }

    override suspend fun getSupportLanguageId(
        authenticationToken: AuthenticationToken?,
        problemId: Int
    ): GetSupportLanguageByProblemIdResp {

        if (!problemRepository.isIdExist(problemId)) {
            return ProblemNotFound
        }

        return SuccessfulGetSupportLanguage(
            problemRepository.getSupportLanguage(problemId)
        )
    }

    override suspend fun create(
        authenticationToken: AuthenticationToken?,
        createSubmissionReq: CreateSubmissionReq
    ): CreateSubmissionResp {

        val language = SupportLanguages.fromId(createSubmissionReq.languageId) ?: return LanguageNotFound
        val user = authentication.validate(authenticationToken) { requireLogin() } ?: return PermissionDenied

        val code = codeRepository.save(
            Code(
                authenticationUserId = user.id,
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
                val user = authentication.validate(authenticationToken) {
                    requireLogin()
                } ?: return PermissionDenied
                if (user.id != codeOwnerId) {
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

        val languageId = codeRepository.getCodeLanguageId(codeId) ?: return LanguageNotFound
        val language = SupportLanguages.fromId(languageId) ?: return LanguageNotFound

        output.onFailure {
            return JudgeResultParseError
        }.onSuccess { output ->
            return SuccessGetOutput(
                when (output) {
                    is Failure.CompileError -> {
                        FailureOutput(
                            CompileError(output.compileResult.files?.get("stderr").toString()),
                            language
                        )
                    }

                    is CompileResultIsNull -> {
                        FailureOutput(
                            CompileResultNotFound,
                            language
                        )
                    }

                    is Failure.RunResultIsNull -> FailureOutput(
                        RunResultIsNull,
                        language
                    )

                    is TargetFileNotExist -> FailureOutput(
                        TargetResultNotFound,
                        language
                    )

                    is Success -> SuccessOutput(
                        stdin = output.runRequest.cmd.first().files?.filterIsInstance<GoJudgeFile.MemoryFile>()
                            ?.first()?.content.toString(),
                        stdout = output.runResult.files?.get("stdout").toString(),
                        language
                    )
                }
            )
        }
        error("这不应该发生")
    }

    override suspend fun getLastNPlaygroundSubmission(
        authenticationToken: AuthenticationToken?,
        last: Int
    ): GetLastNPlaygroundSubmissionResp {
        val user = authentication.getAuthenticationUser(authenticationToken) ?: return PermissionDenied
        return submissionRepository.getByAuthenticationUserID(
            user.id,
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
            return JudgeError(it.cause.toString())
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