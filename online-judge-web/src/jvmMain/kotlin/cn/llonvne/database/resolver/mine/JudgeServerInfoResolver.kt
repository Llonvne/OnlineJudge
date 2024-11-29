package cn.llonvne.database.resolver.mine

import cn.llonvne.kvision.service.IMineService.DashboardResp.JudgeServerInfo
import cn.llonvne.kvision.service.JudgeService
import org.springframework.stereotype.Service

@Service
class JudgeServerInfoResolver(
    private val judgeService: JudgeService,
) {
    suspend fun resolve(): JudgeServerInfo =
        judgeService.info().let {
            JudgeServerInfo(
                name = it.name,
                host = it.host,
                port = it.port,
                cpuCoresCount = it.cpuCoresCount,
                cpuUsage = it.cpuUsage,
                isOnline = it.isOnline,
                memoryUsage = it.memoryUsage,
            )
        }
}
