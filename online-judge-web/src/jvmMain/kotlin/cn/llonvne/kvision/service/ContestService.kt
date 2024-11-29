package cn.llonvne.kvision.service

import cn.llonvne.database.repository.ContestRepository
import cn.llonvne.database.repository.ProblemRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.database.repository.UserRepository
import cn.llonvne.database.resolver.contest.ContestIdGetResolver
import cn.llonvne.database.resolver.contest.ContestProblemVisibilityCheckResolver
import cn.llonvne.database.resolver.contest.ContestProblemVisibilityCheckResolver.ContestProblemVisibilityCheckResult.Pass
import cn.llonvne.database.resolver.contest.ContestProblemVisibilityCheckResolver.ContestProblemVisibilityCheckResult.Reject
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestContext
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.kvision.service.IContestService.AddProblemResp.AddOkResp
import cn.llonvne.security.Token
import cn.llonvne.security.UserLoginLogoutTokenValidator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class ContestService(
    private val authentication: UserLoginLogoutTokenValidator,
    private val problemRepository: ProblemRepository,
    private val contestProblemVisibilityCheckResolver: ContestProblemVisibilityCheckResolver,
    private val contestRepository: ContestRepository,
    private val contestIdGetResolver: ContestIdGetResolver,
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
) : IContestService {
    override suspend fun addProblem(
        value: Token?,
        problemId: String,
    ): IContestService.AddProblemResp {
        val user = authentication.validate(value) { requireLogin() } ?: return PermissionDenied

        val problem =
            problemRepository.getById(problemId.toIntOrNull() ?: return ProblemIdInvalid)
                ?: return ISubmissionService.ProblemNotFound

        when (contestProblemVisibilityCheckResolver.check(user, problem)) {
            Pass -> return AddOkResp(
                problem.problemId ?: return InternalError("Problem Id 不存在"),
                problem.problemName,
            )

            Reject -> return AddProblemPermissionDenied
        }
    }

    override suspend fun create(
        token: Token?,
        createContestReq: IContestService.CreateContestReq,
    ): IContestService.CreateContestResp =
        coroutineScope {
            val userDeferred =
                async(Dispatchers.IO) {
                    authentication.validate(token) {
                        requireLogin()
                    }
                }

            val problemsSet = createContestReq.problems.associateBy { it.problemId }

            val problemsDeferred =
                async {
                    runCatching {
                        createContestReq.problems
                            .asFlow()
                            .cancellable()
                            .buffer(10)
                            .map { problemRepository.getById(it.problemId) }
                            .map { it ?: throw CancellationException("ProblemId 无效") }
                            .catch { e -> cancel(e.message ?: "") }
                            .flowOn(Dispatchers.IO)
                            .toList()
                    }.getOrNull()
                }

            val user = userDeferred.await() ?: return@coroutineScope PermissionDenied
            val problems = problemsDeferred.await() ?: return@coroutineScope ProblemIdInvalid

            val contest =
                contestRepository.create(
                    Contest(
                        ownerId = user.id,
                        title = createContestReq.title,
                        description = createContestReq.description,
                        rankType = createContestReq.rankType,
                        contestScoreType = createContestReq.contestScoreType,
                        startAt = createContestReq.startAt,
                        endAt = createContestReq.endAt,
                        contextStr =
                            ContestContext(
                                problems =
                                    problems.map {
                                        val id = it.problemId ?: return@coroutineScope ProblemIdInvalid
                                        ContestContext.ContestProblem(
                                            id,
                                            problemsSet[id]?.weight ?: return@coroutineScope InternalError("weight 通过非空检查却为空"),
                                            problemsSet[id]?.alias ?: return@coroutineScope InternalError("alias 通过非空检查却为空"),
                                        )
                                    },
                            ).json(),
                        hashLink = UUID.randomUUID().toString(),
                    ),
                )

            return@coroutineScope IContestService.CreateContestResp.CreateOk(contest)
        }

    override suspend fun load(
        value: Token?,
        contestId: ContestId,
    ): IContestService.LoadContestResp {
        val user =
            authentication.validate(value) {
                requireLogin()
            } ?: return PermissionDenied

        val contest = contestIdGetResolver.resolve(contestId) ?: return ContestNotFound

        val username =
            userRepository
                .getByIdOrNull(
                    contest.ownerId,
                )?.username ?: return ContestOwnerNotFound

        return IContestService.LoadContestResp.LoadOk(contest, username)
    }

    override suspend fun contextSubmission(
        value: Token?,
        contestId: ContestId,
    ): IContestService.ContextSubmissionResp {
        val user =
            authentication.validate(value) {
                requireLogin()
            } ?: return PermissionDenied

        val contestId = contestIdGetResolver.resolve(contestId)?.contestId ?: return ContestNotFound

        val submissions = submissionRepository.getByContestId(contestId)

        return IContestService.ContextSubmissionResp.ContextSubmissionOk(submissions)
    }
}
