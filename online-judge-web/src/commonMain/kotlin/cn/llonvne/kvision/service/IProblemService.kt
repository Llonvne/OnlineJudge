package cn.llonvne.kvision.service

import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.problem.context.ProblemContext
import cn.llonvne.security.AuthenticationToken
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
        val memoryLimit: Long
    )

    @Serializable
    sealed interface CreateProblemResp {
        fun isOk() = this is Ok

        @Serializable
        data class Ok(val problemId: Int) : CreateProblemResp

        @Serializable
        data class AuthorIdNotExist(val authorId: Int) : CreateProblemResp
    }


    suspend fun create(authenticationToken: AuthenticationToken?, createProblemReq: CreateProblemReq): CreateProblemResp

    suspend fun list(authenticationToken: AuthenticationToken?): List<ProblemListDto>

    @Serializable
    sealed interface ProblemGetByIdResult {
        @Serializable
        data class Ok(val problem: Problem) : ProblemGetByIdResult

        @Serializable
        data object ProblemNotFound : ProblemGetByIdResult
    }

    suspend fun getById(id: Int): ProblemGetByIdResult
    suspend fun search(token: AuthenticationToken?, text: String): List<ProblemListDto>
}