import cn.llonvne.gojudge.api.*
import cn.llonvne.gojudge.task.gpp.api.GppInput
import cn.llonvne.gojudge.task.gpp.task.GppCompileTask
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GoJudge {
    @Test
    fun testVersion(): Unit {

        val task = GppCompileTask()
        runBlocking {
            println(task.run(GppInput("#include <iostream>\nusing namespace std;\nint main() {\nint a, b;\ncout << 1 << endl\n}"), goJudgeClient))
        }


    }
}
