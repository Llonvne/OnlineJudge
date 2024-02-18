package cn.llonvne

import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.role.CreateTeam
import cn.llonvne.entity.role.TeamSuperManager
import cn.llonvne.security.check
import io.kvision.remote.getAllServiceManagers
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
    listOf(TeamSuperManager.TeamSuperManagerImpl).check(CreateTeam.require(GroupType.Team)).also {
        println(it)
    }

//    runApplication<KVApplication>(*args)
}

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class PingService : IPingService {
    override suspend fun ping(message: String): String = "Hello From Backend"
}



