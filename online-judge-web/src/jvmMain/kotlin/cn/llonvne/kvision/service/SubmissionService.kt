package cn.llonvne.kvision.service

import cn.llonvne.database.repository.*
import cn.llonvne.database.resolver.submission.ProblemJudgeResolver
import cn.llonvne.database.resolver.submission.ProblemSubmissionPassResolver
import cn.llonvne.database.resolver.submission.ProblemSubmissionPersistenceResolver
import cn.llonvne.database.resolver.submission.ProblemSubmissionPersistenceResolver.ProblemSubmissionPersistenceResult.Failed
import cn.llonvne.database.resolver.submission.ProblemSubmissionPersistenceResolver.ProblemSubmissionPersistenceResult.NotNeedToPersist
import cn.llonvne.dtos.AuthenticationUserDto
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.*
import cn.llonvne.entity.problem.context.ProblemTestCases.ProblemTestCase
import cn.llonvne.entity.problem.context.SubmissionTestCases
import cn.llonvne.entity.problem.context.TestCaseType
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeVisibilityType.*
import cn.llonvne.exts.now
import cn.llonvne.getLogger
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.fromId
import cn.llonvne.gojudge.api.spec.runtime.GoJudgeFile
import cn.llonvne.gojudge.api.task.Output.Failure
import cn.llonvne.gojudge.api.task.Output.Failure.CompileResultIsNull
import cn.llonvne.gojudge.api.task.Output.Failure.TargetFileNotExist
import cn.llonvne.gojudge.api.task.Output.Success
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.CreateSubmissionReq.PlaygroundCreateSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.PlaygroundSubmissionDto
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.SuccessGetLastNPlaygroundSubmission
import cn.llonvne.kvision.service.ISubmissionService.GetLastNProblemSubmissionResp.GetLastNProblemSubmissionRespImpl
import cn.llonvne.kvision.service.ISubmissionService.GetSupportLanguageByProblemIdResp.SuccessfulGetSupportLanguage
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto.FailureOutput
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto.FailureReason.*
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto.SuccessOutput
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.SuccessPlaygroundOutput
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp.ProblemSubmissionRespImpl
import cn.llonvne.kvision.service.ISubmissionService.SubmissionGetByIdResp.SuccessfulGetById
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import cn.llonvne.track
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Objects
import kotlin.math.log
import cn.llonvne.database.resolver.submission.ProblemSubmissionPersistenceResolver.ProblemSubmissionPersistenceResult.Success as PersisitSuccess

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
    private val authentication: RedisAuthenticationService,
    private val problemSubmissionPassResolver: ProblemSubmissionPassResolver,
    private val problemJudgeResolver: ProblemJudgeResolver,
    private val problemSubmissionPersistenceResolver: ProblemSubmissionPersistenceResolver
) : ISubmissionService {

    private val json = Json

    private val logger = getLogger()

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

        val result = runCatching {
            submission.result
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
                status = submission.status,
                submissionId = submission.submissionId ?: return SubmissionNotFound,
                judgeResult = result.getOrNull() ?: return JudgeResultParseError
            )
        )
    }

    private suspend fun Submission.aslistDto(): SubmissionGetByIdResp {
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
                    // TODO 获得运行时间和内存
                    runTime = "NULL",
                    runMemory = "NULL",
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
    ): GetJudgeResultByCodeIdResp {
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
            submission.result
        }

        val languageId = codeRepository.getCodeLanguageId(codeId) ?: return LanguageNotFound
        val language = SupportLanguages.fromId(languageId) ?: return LanguageNotFound

        output.onFailure {
            return JudgeResultParseError
        }.onSuccess { output ->
            when (output) {
                is PlaygroundJudgeResult -> {
                    return SuccessPlaygroundOutput(
                        when (val output = output.output) {
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

                is ProblemJudgeResult -> {
                    return ProblemOutput.SuccessProblemOutput(output)
                }
            }
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

        val playgroundJudgeResult = SubmissionTestCases(
            listOf(
                SubmissionTestCases.SubmissionTestCase.from(
                    ProblemTestCase("Playgroud", "Playgroud", req.stdin, "", TestCaseType.OnlyForJudge),
                    output = output.getOrNull()!!
                )
            )
        ).let {
            PlaygroundJudgeResult(it)
        }

        val judgeResult = playgroundJudgeResult.json()

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

    override suspend fun submit(
        value: AuthenticationToken?,
        submissionSubmit: ProblemSubmissionReq
    ): ProblemSubmissionResp {

        val user = authentication.validate(value) {
            requireLogin()
        } ?: return PermissionDenied

        logger.info("判题请求已收到来自用户 ${user.id} 在题目 ${submissionSubmit.problemId} 语言是 ${submissionSubmit.languageId} 代码是 ${submissionSubmit.code}")

        val language = languageRepository.getByIdOrNull(submissionSubmit.languageId).let {
            SupportLanguages.fromId(it?.languageId ?: return LanguageNotFound)
        } ?: return LanguageNotFound

        logger.info("判题语言是 ${language.languageName}:${language.languageVersion}")

        return problemSubmissionPassResolver.resolve(user, submissionSubmit) { problem ->
            val result = problemJudgeResolver.resolve(problem, submissionSubmit, language)

            return@resolve when (val persistenceResult =
                problemSubmissionPersistenceResolver.resolve(user, result, submissionSubmit, language)) {
                is Failed -> {
                    return@resolve InternalError("评测结果持久化失败")
                }

                NotNeedToPersist -> {
                    return@resolve InternalError("评测结果无法持久化,TrackId-${Objects.hash(value, submissionSubmit)}")
                }

                is PersisitSuccess -> {
                    logger.info("成功持久化判题结果,SubmissionId:${persistenceResult.submissionId},CodeId:${persistenceResult.codeId}")
                    ProblemSubmissionRespImpl(
                        persistenceResult.codeId,
                        result.problemTestCases,
                        result.submissionTestCases
                    )
                }
            }
        }
    }

    override suspend fun getLastNProblemSubmission(
        value: AuthenticationToken?,
        problemId: Int,
        lastN: Int
    ): GetLastNProblemSubmissionResp {
        val user = authentication.validate(value) {
            requireLogin()
        } ?: return PermissionDenied

        logger.info("用户 ${user.id} 正在请求题目 $problemId 的前 $lastN 次提交记录")

        val problem = problemRepository.getById(problemId) ?: return ProblemNotFound.also {
            logger.info("题目 $problemId 不存在，请求失败")
        }


        val result = submissionRepository.getByAuthenticationUserID(user.id, codeType = Code.CodeType.Problem, lastN)
            .filter {
                it.problemId != null && it.problemId == problemId
            }.filter {
                it.result is ProblemJudgeResult
            }
            .map { sub ->

                val languageId = codeRepository.getCodeLanguageId(sub.codeId) ?: return LanguageNotFound
                val language = languageRepository.getByIdOrNull(languageId) ?: return LanguageNotFound

                GetLastNProblemSubmissionResp.ProblemSubmissionListDto(
                    language = language,
                    codeId = sub.codeId,
                    submitTime = sub.createdAt ?: LocalDateTime.now(),
                    passerResult = (sub.result as ProblemJudgeResult).passerResult
                )
            }
        return GetLastNProblemSubmissionRespImpl(result).also {
            logger.info("共找到 ${result.size} 条记录")
        }
    }
}