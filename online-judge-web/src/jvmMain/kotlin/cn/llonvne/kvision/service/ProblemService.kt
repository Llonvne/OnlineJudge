package cn.llonvne.kvision.service

import cn.llonvne.database.entity.def.create
import cn.llonvne.database.entity.def.problem
import cn.llonvne.database.service.AuthorRepository
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.types.ProblemStatus
import cn.llonvne.kvision.service.exception.ProblemIdDoNotExistAfterCreation
import cn.llonvne.security.AuthenticationToken
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.core.dsl.query.zip
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class ProblemService(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
    private val authorRepository: AuthorRepository
) :
    IProblemService {
    private val problemMeta = Meta.problem

    override suspend fun list(authenticationToken: AuthenticationToken?): List<ProblemListDto> {
        return db.runQuery {
            QueryDsl.from(problemMeta)
        }
            .map {
                if (authenticationToken == null) {
                    ProblemListDto(it, authorRepository.getByIdOrThrow(it.authorId), ProblemStatus.NotLogin)
                } else {
                    ProblemListDto(it, authorRepository.getByIdOrThrow(it.authorId), ProblemStatus.NotBegin)
                }
            }
    }

    override suspend fun create(
        authenticationToken: AuthenticationToken?,
        createProblemReq: IProblemService.CreateProblemReq
    ): IProblemService.CreateProblemResp {

        if (authenticationToken == null) {
            return IProblemService.CreateProblemResp.PermissionDenied
        }

        if (!authorRepository.isAuthorIdExist(createProblemReq.authorId)) {
            return IProblemService.CreateProblemResp.AuthorIdNotExist(createProblemReq.authorId)
        }

        val problem = db.runQuery {
            QueryDsl.insert(problemMeta).single(
                Problem.create(
                    createProblemReq
                )
            )
        }

        if (problem.problemId == null) {
            throw ProblemIdDoNotExistAfterCreation()
        }

        return IProblemService.CreateProblemResp.Ok(problem.problemId)
    }

    override suspend fun getById(id: Int): IProblemService.ProblemGetByIdResult {
        return db.runQuery {
            QueryDsl.from(problemMeta).where {
                problemMeta.problemId eq id
            }.singleOrNull()
                .map {
                    if (it == null) {
                        IProblemService.ProblemGetByIdResult.ProblemNotFound
                    } else {
                        IProblemService.ProblemGetByIdResult.Ok(it)
                    }
                }
        }
    }
}