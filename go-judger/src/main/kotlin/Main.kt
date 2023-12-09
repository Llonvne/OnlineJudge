import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.app.judging
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName


const val GO_JUDGE_PORT = 5050
const val GO_JUDGE_IP = "localhost"

private const val QUEUE = "demo-queue"

fun main() = SuspendApp {
    resourceScope {
        server(Netty) {
            judging { }
        }
        awaitCancellation()
    }
}



//
//class Kafka private constructor(imageName: DockerImageName) : KafkaContainer(imageName) {
//
//    companion object {
//        private val image: DockerImageName =
//            if (System.getProperty("os.arch") == "aarch64") DockerImageName.parse("niciqy/cp-kafka-arm64:7.0.1")
//                .asCompatibleSubstituteFor("confluentinc/cp-kafka")
//            else DockerImageName.parse("confluentinc/cp-kafka:6.2.1")
//
//        val container: KafkaContainer by lazy {
//            Kafka(image).also { it.start() }
//        }
//    }
//}