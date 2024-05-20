package cn.llonvne.kvision.service

import cn.llonvne.database.entity.def.problem.fromCreateReq
import cn.llonvne.database.repository.AuthorRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.dtos.ProblemForList
import cn.llonvne.entity.problem.PlaygroundJudgeResult
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.problem.ProblemListShowType
import cn.llonvne.entity.problem.ProblemListShowType.*
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.kvision.service.IProblemService.CreateProblemResp.AuthorIdNotExist
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.ProblemNotFound
import cn.llonvne.kvision.service.exception.ProblemIdDoNotExistAfterCreation
import cn.llonvne.security.Token
import cn.llonvne.security.UserLoginLogoutTokenValidator
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class ProblemService(
    private val authorRepository: AuthorRepository,
    private val problemRepository: ProblemRepository,
    private val submissionRepository: SubmissionRepository,
    private val languageRepository: LanguageRepository,
    private val authentication: UserLoginLogoutTokenValidator
) : IProblemService {

    override suspend fun list(
        token: Token?,
        showType: ProblemListShowType
    ): List<ProblemForList> {
        return problemRepository.list()
            .filter { problem ->
                when (showType) {
                    All -> true
                    Accepted -> {
                        val user = authentication.validate(token) {
                            requireLogin()
                        } ?: return listOf()
                        val submission = submissionRepository.getByAuthenticationUserID(
                            user.id,
                            codeType = Code.CodeType.Problem
                        )
                        val set = submission.filter {
                            when (it.result) {
                                is PlaygroundJudgeResult -> false
                                is ProblemJudgeResult -> (it.result as ProblemJudgeResult).passerResult.pass
                            }
                        }
                            .mapNotNull { it.problemId }.toSet()
                        problem.problemId in set
                    }

                    Attempted -> {
                        val user = authentication.validate(token) {
                            requireLogin()
                        } ?: return listOf()
                        val submission = submissionRepository.getByAuthenticationUserID(
                            user.id,
                            codeType = Code.CodeType.Problem
                        )
                        val set = submission.filter {
                            when (it.result) {
                                is PlaygroundJudgeResult -> false
                                is ProblemJudgeResult -> true
                            }
                        }
                            .mapNotNull { it.problemId }.toSet()
                        problem.problemId in set
                    }

                    Favorite -> {
                        TODO()
                    }
                }
            }
            .mapNotNull { it.listDto(token) }
    }

    override suspend fun create(
        token: Token?, createProblemReq: IProblemService.CreateProblemReq
    ): IProblemService.CreateProblemResp {

        if (token == null) {
            return PermissionDenied
        }

        if (!authorRepository.isAuthorIdExist(createProblemReq.authorId)) {
            return AuthorIdNotExist(createProblemReq.authorId)
        }

        val problem = problemRepository.create(
            Problem.fromCreateReq(
                createProblemReq,
                ownerId = token.id
            )
        )

        if (problem.problemId == null) {
            throw ProblemIdDoNotExistAfterCreation()
        }

        languageRepository.setSupportLanguages(problem.problemId,
            createProblemReq.supportLanguages.map { it.languageId })

        return IProblemService.CreateProblemResp.Ok(problem.problemId)
    }

    override suspend fun getById(id: Int): ProblemGetByIdResult {
        val problem = problemRepository.getById(id)
        return if (problem == null) {
            ProblemNotFound
        } else {
            GetProblemByIdOk(problem, problemRepository.getSupportLanguage(id), problemRepository.getProblemTags(id))
        }
    }

    override suspend fun search(token: Token?, text: String): List<ProblemForList> {
        return problemRepository.search(text).mapNotNull {
            it.listDto(token)
        }
    }

    private suspend fun Problem.listDto(token: Token?): ProblemForList? {
        return onIdNotNull(null) { id, problem ->
            ProblemForList(
                problem,
                id,
                authorRepository.getByIdOrThrow(authorId),
                submissionRepository.getUserProblemStatus(token, id),
                problemRepository.getProblemTags(id)
            )
        }
    }
}
