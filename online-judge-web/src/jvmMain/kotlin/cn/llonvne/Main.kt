package cn.llonvne

import cn.llonvne.entity.role.Role
import cn.llonvne.entity.role.TeamIdRole
import io.kvision.remote.getAllServiceManagers
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



