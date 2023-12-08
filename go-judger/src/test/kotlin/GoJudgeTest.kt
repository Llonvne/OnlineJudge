import cn.llonvne.gojudge.api.GppCompileTask
import cn.llonvne.gojudge.api.GppInput
import cn.llonvne.gojudge.api.version
import cn.llonvne.gojudge.service.goJudgeService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GoJudgeTest {
    @Test
    fun test() = runBlocking {
        println(goJudgeService.version())
        goJudgeService.version()

        val gpp = GppCompileTask()
        val result = gpp.run(
            GppInput(
                """
                    #include <iostream>

                    int main() {
                        int a;
                        std::cin >> a;
                        std::cout << a << std::endl;
                        return 0;
                    }
                """.trimIndent(), "100"
            ), goJudgeService
        )
        println(result)
        Unit
    }


}