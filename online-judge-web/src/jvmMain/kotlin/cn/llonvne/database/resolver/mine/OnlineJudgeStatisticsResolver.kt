package cn.llonvne.database.resolver.mine

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.ContestRepository
import cn.llonvne.database.repository.SubmissionRepository
import cn.llonvne.exts.now
import cn.llonvne.kvision.service.IMineService
import cn.llonvne.kvision.service.IMineService.DashboardResp.OnlineJudgeStatistics
import kotlinx.datetime.*
import org.springframework.stereotype.Service

@Service
class OnlineJudgeStatisticsResolver(
    private val userRepository: AuthenticationUserRepository,
    private val submissionRepository: SubmissionRepository,
    private val contestRepository: ContestRepository
) {
    suspend fun resolve(): OnlineJudgeStatistics {
        val todayEnd = LocalDateTime.now()
        val todayStart = todayEnd.toJavaLocalDateTime().minusDays(1).toKotlinLocalDateTime()

        return OnlineJudgeStatistics(
            totalUserCount = userRepository.count(),
            totalSubmissionToday = submissionRepository.getByTimeRange(
                todayStart,
                todayEnd
            ).size,
            totalContestLastTwoWeek = contestRepository.lastTwoWeekCount()
        )
    }


}