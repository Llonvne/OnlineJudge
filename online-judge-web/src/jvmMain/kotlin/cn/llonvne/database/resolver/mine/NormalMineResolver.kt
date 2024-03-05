package cn.llonvne.database.resolver.mine

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.exts.now
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.kvision.service.IAuthenticationService.MineResp.NormalUserMineResp
import cn.llonvne.ll
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.springframework.stereotype.Service

@Service
class NormalMineResolver(
    private val userSubmissionRangeResolver: UserSubmissionRangeResolver
) {
    suspend fun resolve(user: AuthenticationUser): IAuthenticationService.MineResp {

        return coroutineScope {
            val total = async {
                userSubmissionRangeResolver.resolve(
                    user, java.time.LocalDateTime.MIN.toKotlinLocalDateTime()
                ).filter {
                    it.result is ProblemJudgeResult
                }.count {
                    (it.result as ProblemJudgeResult).passerResult.pass
                }
            }
            val at7Days = async { userSubmissionRangeResolver.acceptedIn(user, 7) }
            val atToday = async { userSubmissionRangeResolver.acceptedIn(user, 1) }
            val at30Day = async { userSubmissionRangeResolver.acceptedIn(user, 30) }

            return@coroutineScope NormalUserMineResp(
                user.username, user.createdAt?.ll() ?: LocalDateTime.now().ll(),
                acceptedTotal = total.await(),
                accepted7Days = at7Days.await(),
                acceptedToday = atToday.await(),
                accepted30Days = at30Day.await()
            )
        }
    }
}