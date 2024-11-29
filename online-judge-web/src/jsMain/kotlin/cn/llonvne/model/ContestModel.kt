package cn.llonvne.model

import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestContext
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.kvision.service.IContestService
import cn.llonvne.site.contest.CreateContestForm
import io.kvision.remote.getService
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

object ContestModel {
    private val contestService = getService<IContestService>()

    suspend fun addProblem(problemId: String) =
        contestService.addProblem(
            AuthenticationModel.userToken.value,
            problemId,
        )

    suspend fun create(
        createContestForm: CreateContestForm,
        problems: List<ContestContext.ContestProblem>,
    ): IContestService.CreateContestResp =
        contestService.create(
            token = AuthenticationModel.userToken.value,
            IContestService.CreateContestReq(
                title = createContestForm.title,
                description = createContestForm.description,
                startAt =
                    createContestForm.startAt
                        .toKotlinInstant()
                        .toLocalDateTime(timeZone = TimeZone.currentSystemDefault()),
                endAt =
                    createContestForm.endAt
                        .toKotlinInstant()
                        .toLocalDateTime(timeZone = TimeZone.currentSystemDefault()),
                contestScoreType = Contest.ContestScoreType.valueOf(createContestForm.contestScoreTypeStr),
                problems = problems,
                rankType = Contest.ContestRankType.valueOf(createContestForm.rankTypeStr),
            ),
        )

    suspend fun load(contestId: ContestId) =
        contestService.load(
            AuthenticationModel.userToken.value,
            contestId,
        )

    suspend fun contextSubmissions(contestId: ContestId) =
        contestService.contextSubmission(
            AuthenticationModel.userToken.value,
            contestId,
        )
}
