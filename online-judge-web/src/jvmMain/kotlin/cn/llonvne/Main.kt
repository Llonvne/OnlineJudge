package cn.llonvne

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.role.CreateTeam
import cn.llonvne.entity.role.TeamManager
import cn.llonvne.exts.now
import cn.llonvne.security.UserRole
import cn.llonvne.security.check
import cn.llonvne.security.normalUserRole
import io.kvision.remote.getAllServiceManagers
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication(
    exclude = [org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration::class,
        org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration::class]
)
class KVApplication {
    @Bean
    fun getManagers() = getAllServiceManagers()
}

fun main(args: Array<String>) {
    runApplication<KVApplication>(*args)
}

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class PingService : IPingService {
    override suspend fun ping(message: String): String = "Hello From Backend"
}



