package cn.llonvne.kvision.service

import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestContext
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.problem.Submission
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.datetime.LocalDateTime

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@KVService
interface IContestService {
    @Serializable
    sealed interface AddProblemResp {
        @Serializable
        data class AddOkResp(val problemId: Int, val problemName: String) : AddProblemResp
    }

    suspend fun addProblem(value: AuthenticationToken?, problemId: String): AddProblemResp

    @Serializable
    data class CreateContestReq(
        val title: String,
        val description: String = "",
        @Contextual val startAt: LocalDateTime,
        @Contextual val endAt: LocalDateTime,
        val contestScoreType: Contest.ContestScoreType,
        val rankType: Contest.ContestRankType,
        val problems: List<ContestContext.ContestProblem>
    )

    @Serializable
    sealed interface CreateContestResp {
        @Serializable
        data class CreateOk(val contest: Contest) : CreateContestResp
    }

    suspend fun create(authenticationToken: AuthenticationToken?, createContestReq: CreateContestReq): CreateContestResp

    @Serializable
    sealed interface LoadContestResp {
        @Serializable
        data class LoadOk(val contest: Contest, val ownerName: String) : LoadContestResp
    }

    suspend fun load(value: AuthenticationToken?, contestId: ContestId): LoadContestResp


    @Serializable
    sealed interface ContextSubmissionResp {
        @Serializable
        data class ContextSubmissionOk(val submissions: List<Submission>) : ContextSubmissionResp
    }

    suspend fun contextSubmission(value: AuthenticationToken?, contestId: ContestId): ContextSubmissionResp
}