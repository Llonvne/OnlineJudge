package cn.llonvne

import cn.llonvne.gojudge.api.Sample
import de.jensklingenberg.ktorfit.Ktorfit
import io.kvision.remote.getAllServiceManagers
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@SpringBootApplication(
    exclude = [
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration::class,
        org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration::class
    ]
)
class KVApplication {
    @Bean
    fun getManagers() = getAllServiceManagers()

    @Bean
    fun sample() = Ktorfit.Builder()
        .baseUrl("http://localhost:8081/")
        .build().create<Sample>()
}

fun main(args: Array<String>) {
    runApplication<KVApplication>(*args)
}


@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class PingService(private val sample: Sample) : IPingService {

    override suspend fun ping(message: String): String {
        return sample.version()
    }
}

