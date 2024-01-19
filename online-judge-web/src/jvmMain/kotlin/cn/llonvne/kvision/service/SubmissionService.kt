package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.dtos.AuthenticationUserDto
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.Submission
import cn.llonvne.exts.now
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.api.task.Output.Companion.formatOnSuccess
import cn.llonvne.gojudge.api.task.format
import cn.llonvne.kvision.service.ISubmissionService.SubmissionGetByIdResp
import cn.llonvne.kvision.service.ISubmissionService.SubmissionGetByIdResp.SuccessfulGetById
import cn.llonvne.security.AuthenticationToken
import kotlinx.datetime.LocalDateTime
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
    private val authenticationUserRepository: AuthenticationUserRepository
) : ISubmissionService {
    override suspend fun list(req: ISubmissionService.ListSubmissionReq): List<SubmissionListDto> {

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
            submissionRepository.getById(id) ?: return ISubmissionService.SubmissionNotFound

        return submission.toSubmissionListDto()
    }

    override suspend fun getViewCode(id: Int): ISubmissionService.ViewCodeGetByIdResp {
        val submission = submissionRepository.getById(id) ?: return ISubmissionService.SubmissionNotFound

        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(submission.judgeResult)
        }

        val problem = problemRepository.getById(submission.problemId) ?: return LanguageNotFound

        return ISubmissionService.ViewCodeGetByIdResp.SuccessfulGetById(
            ViewCodeDto(
                rawCode = submission.rowCode,
                language = languageRepository.getByIdOrNull(submission.languageId)
                    ?: return LanguageNotFound,
                problemName = problem.problemName,
                problemId = problem.problemId ?: return ISubmissionService.ProblemNotFound,
                output = result.getOrNull(),
                status = submission.status,
                submissionId = submission.submissionId ?: return ISubmissionService.SubmissionNotFound
            )
        )
    }

    private suspend fun Submission.toSubmissionListDto(): SubmissionGetByIdResp {
        val result: Result<Output> = kotlin.runCatching {
            Json.decodeFromString(judgeResult)
        }

        val problem = problemRepository.getById(problemId) ?: return ISubmissionService.ProblemNotFound

        return SuccessfulGetById(
            SubmissionListDto(
                language = languageRepository.getByIdOrNull(languageId) ?: return LanguageNotFound,
                user = AuthenticationUserDto(
                    authenticationUserRepository.getByIdOrNull(authenticationUserId)?.username
                        ?: return ISubmissionService.UserNotFound,
                ),
                problemId = problemId,
                problemName = problem.problemName,
                submissionId = this.submissionId ?: return ISubmissionService.SubmissionNotFound,
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
                codeLength = this.rowCode.length.toLong(),
                submitTime = this.createdAt ?: LocalDateTime.now()
            )
        )
    }

    override suspend fun getSupportLanguageId(
        authenticationToken: AuthenticationToken?,
        problemId: Int
    ): ISubmissionService.GetSupportLanguageByProblemIdResp {
        if (!problemRepository.isIdExist(problemId)) {
            return ISubmissionService.ProblemNotFound
        }

        return ISubmissionService.GetSupportLanguageByProblemIdResp.SuccessfulGetSupportLanguage(
            problemRepository.getSupportLanguage(problemId)
        )
    }
}