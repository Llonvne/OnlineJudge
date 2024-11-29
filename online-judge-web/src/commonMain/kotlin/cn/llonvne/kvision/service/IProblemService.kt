package cn.llonvne.kvision.service

import cn.llonvne.dtos.ProblemForList
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.ProblemListShowType
import cn.llonvne.entity.problem.ProblemTag
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.context.ProblemContext
import cn.llonvne.entity.problem.context.ProblemType
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.security.Token
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface IProblemService {
    @Serializable
    data class CreateProblemReq(
        val problemName: String,
        val problemDescription: String,
        val problemContext: ProblemContext,
        val authorId: Int,
        val timeLimit: Long,
        val memoryLimit: Long,
        val visibility: ProblemVisibility,
        val type: ProblemType,
        val supportLanguages: List<Language>,
    )

    @Serializable
    sealed interface CreateProblemResp {
        @Serializable
        data class Ok(
            val problemId: Int,
        ) : CreateProblemResp

        @Serializable
        data class AuthorIdNotExist(
            val authorId: Int,
        ) : CreateProblemResp
    }

    suspend fun create(
        token: Token?,
        createProblemReq: CreateProblemReq,
    ): CreateProblemResp

    suspend fun list(
        token: Token?,
        showType: ProblemListShowType,
    ): List<ProblemForList>

    @Serializable
    sealed interface ProblemGetByIdResult {
        @Serializable
        data class GetProblemByIdOk(
            val problem: Problem,
            val supportLanguages: List<Language>,
            val tage: List<ProblemTag>,
        ) : ProblemGetByIdResult

        @Serializable
        data object ProblemNotFound : ProblemGetByIdResult
    }

    suspend fun getById(id: Int): ProblemGetByIdResult

    suspend fun search(
        token: Token?,
        text: String,
    ): List<ProblemForList>
}
