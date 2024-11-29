@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package cn.llonvne.database.resolver.mine

import cn.llonvne.kvision.service.IMineService.DashboardResp.BackendInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.lang.management.ManagementFactory
import java.net.InetAddress

@Service
class BackendInfoResolver(
    env: Environment,
    private val backendName: String = env.getProperty("oj.name") ?: throw RuntimeException("后端未定义"),
    private val port: String = env.getProperty("server.port") ?: "8080",
    private val runtime: Runtime = Runtime.getRuntime(),
) {
    private val availableProcessors by lazy {
        runtime.availableProcessors()
    }

    suspend fun resolve(): BackendInfo =
        BackendInfo(
            name = backendName,
            host =
                withContext(Dispatchers.IO) {
                    InetAddress.getLocalHost()
                }.hostAddress,
            port = port,
            cpuCoresCount = availableProcessors,
            cpuUsage = ManagementFactory.getOperatingSystemMXBean().systemLoadAverage,
            usedMemory = runtime.maxMemory().toInt() - runtime.freeMemory().toInt(),
            totalMemory = runtime.maxMemory().toInt(),
            isOnline = true,
        )
}
