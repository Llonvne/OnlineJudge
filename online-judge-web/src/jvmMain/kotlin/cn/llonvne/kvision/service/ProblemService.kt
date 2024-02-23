package cn.llonvne.kvision.service

import cn.llonvne.database.entity.def.problem.fromCreateReq
import cn.llonvne.database.repository.AuthorRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.kvision.service.IProblemService.CreateProblemResp.AuthorIdNotExist
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.ProblemNotFound
import cn.llonvne.kvision.service.exception.ProblemIdDoNotExistAfterCreation
import cn.llonvne.security.AuthenticationToken
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
    private val languageRepository: LanguageRepository
) : IProblemService {

    override suspend fun list(authenticationToken: AuthenticationToken?): List<ProblemListDto> {
        return problemRepository.list().mapNotNull { it.listDto(authenticationToken) }
    }

    override suspend fun create(
        authenticationToken: AuthenticationToken?, createProblemReq: IProblemService.CreateProblemReq
    ): IProblemService.CreateProblemResp {

        if (authenticationToken == null) {
            return PermissionDenied
        }

        if (!authorRepository.isAuthorIdExist(createProblemReq.authorId)) {
            return AuthorIdNotExist(createProblemReq.authorId)
        }

        val problem = problemRepository.create(
            Problem.fromCreateReq(
                createProblemReq,
                ownerId = authenticationToken.id
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

    override suspend fun search(token: AuthenticationToken?, text: String): List<ProblemListDto> {
        return problemRepository.search(text).mapNotNull {
            it.listDto(token)
        }
    }

    private suspend fun Problem.listDto(token: AuthenticationToken?): ProblemListDto? {
        return onIdNotNull(null) { id, problem ->
            ProblemListDto(
                problem,
                id,
                authorRepository.getByIdOrThrow(authorId),
                submissionRepository.getUserProblemStatus(token, id),
                problemRepository.getProblemTags(id)
            )
        }
    }
}
