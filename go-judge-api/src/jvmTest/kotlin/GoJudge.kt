import cn.llonvne.gojudge.api.Cmd
import cn.llonvne.gojudge.api.GoJudgeFile
import cn.llonvne.gojudge.api.RequestType
import cn.llonvne.gojudge.api.goJudgeClient
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.max
import kotlin.test.Test

class GoJudge {
    @Test
    fun testVersion(): Unit {
        runBlocking {
            println(
                goJudgeClient.run(
                    RequestType.Request(
                        cmd = listOf(
//                            Cmd(
//                                args = listOf("/usr/bin/g++", "a.cc", "-o", "a"),
//                                env = listOf("PATH=/usr/bin:/bin"),
//                                files = listOf(
//                                    GoJudgeFile.Collector(
//                                        name = "stderr",
//                                        max = 10240
//                                    ),
//                                    GoJudgeFile.Collector(
//                                        name = "stdout",
//                                        max = 10240
//                                    )
//                                ),
//                                procLimit = 50,
//                                memoryLimit = 104857600,
//                                cpuLimit = 10000000000,
//                                copyIn = mapOf(
//                                    "a.cc" to GoJudgeFile.MemoryFile(
//                                        content = "#include <iostream>\nusing namespace std;\nint main() {\nint a, b;\ncout << 1 << endl;\n}"
//                                    )
//                                ),
//                                copyOut = listOf("stdout", "stderr"),
//                                copyOutCached = listOf("a.cc","a")
//                            ),
                            Cmd(
                                args = listOf("a"),
                                env = listOf("PATH=/usr/bin:/bin"),
                                files = listOf(
                                    GoJudgeFile.Collector(
                                        name = "stdout",
                                        max = 10240
                                    ),
                                    GoJudgeFile.Collector(
                                        name = "stderr",
                                        max = 10240
                                    )
                                ),
                                procLimit = 50,
                                memoryLimit = 104857600,
                                cpuLimit = 10000000000,
                                copyIn = mapOf(
                                    "a" to GoJudgeFile.PreparedFile(fileId = "GMCULKCQ")
                                )
                            )
                        ),
                    )
                )
            )
        }
    }
}
