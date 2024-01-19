package cn.llonvne.kvision.service

import cn.llonvne.database.entity.def.problem.fromCreateReq
import cn.llonvne.database.repository.AuthorRepository
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.types.ProblemStatus
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
    private val problemRepository: ProblemRepository
) :
    IProblemService {

    override suspend fun list(authenticationToken: AuthenticationToken?): List<ProblemListDto> {
        return problemRepository.list().mapNotNull { it.toProblemListDto(authenticationToken) }
    }

    override suspend fun create(
        authenticationToken: AuthenticationToken?,
        createProblemReq: IProblemService.CreateProblemReq
    ): IProblemService.CreateProblemResp {

        if (authenticationToken == null) {
            return PermissionDenied
        }

        if (!authorRepository.isAuthorIdExist(createProblemReq.authorId)) {
            return IProblemService.CreateProblemResp.AuthorIdNotExist(createProblemReq.authorId)
        }

        val problem = problemRepository.create(Problem.fromCreateReq(createProblemReq))

        if (problem.problemId == null) {
            throw ProblemIdDoNotExistAfterCreation()
        }

        return IProblemService.CreateProblemResp.Ok(problem.problemId)
    }

    override suspend fun getById(id: Int): IProblemService.ProblemGetByIdResult {
        val problem = problemRepository.getById(id)
        return if (problem == null) {
            IProblemService.ProblemGetByIdResult.ProblemNotFound
        } else {
            IProblemService.ProblemGetByIdResult.Ok(problem)
        }
    }

    override suspend fun search(token: AuthenticationToken?, text: String): List<ProblemListDto> {
        return problemRepository.search(text)
            .mapNotNull {
                it.toProblemListDto(token)
            }
    }

    private suspend fun Problem.toProblemListDto(token: AuthenticationToken?): ProblemListDto? {
        return if (token == null) {
            ProblemListDto(
                this,
                this.problemId ?: return null,
                authorRepository.getByIdOrThrow(authorId),
                ProblemStatus.NotLogin,
                problemRepository.getProblemTags(problemId)
            )
        } else {
            ProblemListDto(
                this,
                this.problemId ?: return null,
                authorRepository.getByIdOrThrow(authorId),
                ProblemStatus.NotBegin,
                problemRepository.getProblemTags(problemId)
            )
        }
    }
}
