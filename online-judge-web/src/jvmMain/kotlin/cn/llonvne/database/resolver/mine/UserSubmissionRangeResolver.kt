package cn.llonvne.database.resolver.mine

import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.exts.now
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.springframework.stereotype.Service

@Service
class UserSubmissionRangeResolver(
    private val submissionRepository: SubmissionRepository,
) {
    suspend fun resolve(
        authenticationUser: AuthenticationUser,
        from: LocalDateTime,
        to: LocalDateTime = LocalDateTime.now(),
    ): List<Submission> =
        submissionRepository
            .getByAuthenticationUserID(
                authenticationUser.id,
                codeType = Code.CodeType.Problem,
            ).filter {
                it.createdAt != null && it.createdAt in from..to
            }

    suspend fun acceptedIn(
        user: AuthenticationUser,
        days: Long,
    ): Int =
        resolve(
            user,
            java.time.LocalDateTime
                .now()
                .minusDays(days)
                .toKotlinLocalDateTime(),
        ).filter {
            it.result is ProblemJudgeResult
        }.count {
            (it.result as ProblemJudgeResult).passerResult.pass
        }
}
